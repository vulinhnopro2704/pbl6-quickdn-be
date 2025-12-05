package com.pbl6.order.service;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.*;
import com.pbl6.order.event.OrderCreatedEvent;
import com.pbl6.order.exception.AppException;
import com.pbl6.order.mapper.OrderMapper;
import com.pbl6.order.repository.*;
import com.pbl6.order.spec.OrderSpecifications;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import static com.pbl6.order.constant.RedisKeyConstants.*;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepo;
  private final PackageRepository packageRepo;
  private final PackageAddressRepository addressRepo;
  private final PricingService pricingService;
  private final OrderStatusHistoryRepository historyRepo;
  private final GooongMapClientService gooongMapClient;
  private final ShippingConfigRepository shippingConfigRepo;
  private final SizeConfigRepository sizeConfigRepo;
  private final ApplicationEventPublisher publisher;
  private final RedisTemplate<String, String> redisTemplate;
  private final FirebaseMessagingService firebaseMessagingService;
  private final ExecutorService pushExecutor;

  @Transactional
  public CreateOrderResponse createOrder(CreateOrderRequest req) {
    // get creator id or phone from security context (subject)
    String creatorSubject = SecurityContextHolder.getContext().getAuthentication().getName();
    UUID creatorId = null;
    try {
      creatorId = UUID.fromString(creatorSubject);
    } catch (Exception ignored) {
    }

    // create or reuse pickup address
    PackageAddressEntity pickup = createAddress(req.pickupAddress());

    OrderEntity order = new OrderEntity();
    if (creatorId != null) order.setCreatorId(creatorId);
    order.setPickupAddress(pickup);
    order.setCustomerNote(req.customerNote());
    if (req.scheduledAt() != null) {
      try {
        order.setScheduledAt(LocalDateTime.parse(req.scheduledAt())); // expects ISO-8601
      } catch (DateTimeParseException ex) {
        throw AppException.badRequest("scheduledAt phải là ISO-8601");
      }
    }

    // packages
    List<PackageEntity> packageEntities = new ArrayList<>();
    // dùng BigDecimal cho tổng khoảng cách và tổng tiền
    BigDecimal estimatedDistanceKm = BigDecimal.ZERO;
    int estimatedDurationMin = 0;
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (PackageDto packageDto : req.packages()) {
      PackageAddressEntity dropAddr = createAddress(packageDto.receiverAddress());

      PackageEntity pkg = new PackageEntity();
      pkg.setOrder(order);
      pkg.setDropoffAddress(dropAddr);

      // chuyển đổi các giá trị number sang BigDecimal nếu entity đã dùng BigDecimal
      if (packageDto.weightKg() != null) {
        pkg.setWeightKg(BigDecimal.valueOf(packageDto.weightKg()));
      } else {
        pkg.setWeightKg(BigDecimal.ZERO);
      }

      pkg.setPackageSize(packageDto.size());
      pkg.setPayerType(packageDto.payerType());
      pkg.setDescription(packageDto.description());
      pkg.setImageUrl(packageDto.imageUrl());
      if (packageDto.category() != null) {
        pkg.setCategory(packageDto.category());
      }
      Boolean cod = packageDto.cod();
      Double codAmount = packageDto.codAmount();

      if (cod != null && cod) {
        if (codAmount == null || codAmount <= 0.0) {
          throw AppException.badRequest("Khi chọn COD, codAmount phải > 0");
        }
        // convert to BigDecimal và set
        pkg.setCodFee(BigDecimal.valueOf(codAmount));
      } else {
        if (codAmount != null && codAmount > 0.0) {
          // business rule: không cho phép gửi codAmount nếu client không tick COD
          throw AppException.badRequest("Không được cung cấp codAmount khi COD không được chọn");
        }
        pkg.setCodFee(BigDecimal.ZERO);
      }
      packageEntities.add(pkg);
    }

    if (req.voucherCode() != null) {
      // Ap dung logic voucher o day neu can
      if (!req.voucherCode().equals("VALID-VOUCHER")) {
        throw AppException.badRequest("Mã voucher không hợp lệ");
      }
    }

    if (req.paymentMethod() != null) {
      order.setPaymentMethod(req.paymentMethod());
    } else {
      order.setPaymentMethod(PaymentMethod.CASH); // default
    }

    List<PriceAndRouteDto> priceAndRouteDtos = computePriceAndRouteForOrder(req);
    Map<Integer, PriceAndRouteDto> indexToPriceRoute = new HashMap<>();
    for (PriceAndRouteDto dto : priceAndRouteDtos) {
      indexToPriceRoute.put(dto.packageIndex(), dto);
    }

    for (int index = 0; index < packageEntities.size(); index++) {
      PackageEntity pkg = packageEntities.get(index);
      PriceAndRouteDto dto = indexToPriceRoute.get(index + 1); // packageIndex is 1-based
      if (dto == null) {
        throw AppException.badRequest(
            "Không tìm thấy thông tin giá và lộ trình cho gói hàng thứ " + (index + 1));
      }

      pkg.setDeliveryFee(BigDecimal.valueOf(dto.price()));
      pkg.setEstimatedDistanceKm(BigDecimal.valueOf((double) dto.distance() / 1000));
      pkg.setEstimatedDurationMin(
          (int) Math.round((double) dto.estimatedDuration() / 60)); // convert giây sang phút
      // cập nhật estimatedDistanceKm và estimatedDurationMin
      estimatedDistanceKm = estimatedDistanceKm.max(pkg.getEstimatedDistanceKm());
      estimatedDurationMin = Math.max(estimatedDurationMin, pkg.getEstimatedDurationMin());

      // cộng dồn vào totalAmount của order
      totalAmount = totalAmount.add(pkg.getDeliveryFee());
    }
    order.setPackages(packageEntities);
    order.setEstimatedDistanceKm(estimatedDistanceKm);
    order.setEstimatedDurationMin(estimatedDurationMin);
    order.setTotalAmount(totalAmount);

    if (!priceAndRouteDtos.isEmpty()) {
      for (PriceAndRouteDto pr : priceAndRouteDtos) {
        OrderPriceRouteEntity route = new OrderPriceRouteEntity();
        route.setOrder(order); // bắt buộc để JPA thiết lập FK
        route.setPrice(BigDecimal.valueOf(pr.price()));
        route.setLatitude(BigDecimal.valueOf(pr.latitude()));
        route.setLongitude(BigDecimal.valueOf(pr.longitude()));
        route.setRouteIndex(pr.routeIndex());
        route.setPackageIndex(pr.packageIndex());
        route.setDistance(pr.distance());
        route.setEstimatedDuration(pr.estimatedDuration());
        order.getPriceRoutes().add(route);
      }
    }

    if (req.scheduledAt() != null) {
      order.setScheduledAt(LocalDateTime.parse(req.scheduledAt()));
    }

    // SAVE order (packages sẽ được persist bởi cascade = CascadeType.ALL)
    orderRepo.save(order);
    // PUBLISH event AFTER commit via @TransactionalEventListener (we still publish inside tx)
    double lon =
        order.getPickupAddress() != null && order.getPickupAddress().getLongitude() != null
            ? order.getPickupAddress().getLongitude().doubleValue()
            : 0.0;
    double lat =
        order.getPickupAddress() != null && order.getPickupAddress().getLatitude() != null
            ? order.getPickupAddress().getLatitude().doubleValue()
            : 0.0;

    publisher.publishEvent(new OrderCreatedEvent(order.getId(), lon, lat));
    // trả về totalAmount (BigDecimal). CreateOrderResponse now uses OrderStatus enum for status
    return new CreateOrderResponse(
        order.getId(), order.getTotalAmount().doubleValue(), "VND", order.getStatus());
  }

  private PackageAddressEntity createAddress(AddressDto addrDto) {
    PackageAddressEntity addr = new PackageAddressEntity();
    addr.setDetail(addrDto.detail());
    addr.setName(addrDto.name());
    addr.setPhone(addrDto.phone());
    addr.setNote(addrDto.note());
    //    addr.setWardCode(addrDto.wardCode());
    //    addr.setDistrictCode(addrDto.districtCode());

    // Entity dùng BigDecimal cho latitude/longitude => convert an toàn (null check)
    if (addrDto.latitude() != null) {
      addr.setLatitude(BigDecimal.valueOf(addrDto.latitude()));
    } else {
      addr.setLatitude(null);
    }

    if (addrDto.longitude() != null) {
      addr.setLongitude(BigDecimal.valueOf(addrDto.longitude()));
    } else {
      addr.setLongitude(null);
    }

    addressRepo.save(addr);
    return addr;
  }

  public Page<OrderDetailResponse> getOrdersByRoleSelector(
      UUID currentUserId,
      Set<String> actualRoles,
      String roleToUse, // e.g. "USER", "DRIVER", "ADMIN", or null (auto)
      UUID filterUserId, // only meaningful if roleToUse == ADMIN
      String q,
      String status,
      String paymentMethod,
      LocalDate fromDate,
      LocalDate toDate,
      Pageable pageable) {
    // If roleToUse is explicitly admin, ensure actualRoles contains it (controller checked already)
    if ("ADMIN".equals(roleToUse)) {
      Specification<OrderEntity> spec = (root, query, cb) -> null;
      if (filterUserId != null) spec = spec.and(OrderSpecifications.belongsToUser(filterUserId));
      // add common filters
      spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
      return orderRepo.findAll(spec, pageable).map(OrderMapper::toDetail);
    }

    // If roleToUse is explicitly DRIVER
    if ("DRIVER".equals(roleToUse)) {
      if (currentUserId == null) throw AppException.badRequest("Không xác định driverId từ token");
      Specification<OrderEntity> spec = OrderSpecifications.hasShipperId(currentUserId);
      spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
      return orderRepo.findAll(spec, pageable).map(OrderMapper::toDetail);
    }

    // If roleToUse is explicitly USER
    if ("USER".equals(roleToUse)) {
      if (currentUserId == null) throw AppException.badRequest("Không xác định userId từ token");
      Specification<OrderEntity> spec = OrderSpecifications.belongsToUser(currentUserId);
      spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
      return orderRepo.findAll(spec, pageable).map(OrderMapper::toDetail);
    }

    // roleToUse == null => auto / union behavior based on actualRoles
    // Build OR predicates for roles the user actually has (e.g. USER and/or DRIVER)
    Specification<OrderEntity> spec = getOrderEntitySpecification(currentUserId, actualRoles);
    spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
    return orderRepo.findAll(spec, pageable).map(OrderMapper::toDetail);
  }

  private static Specification<OrderEntity> getOrderEntitySpecification(
      UUID currentUserId, Set<String> actualRoles) {
    Specification<OrderEntity> roleSpec =
        (root, query, cb) -> {
          Predicate or = cb.disjunction();
          if (actualRoles.contains("USER")) {
            if (currentUserId == null)
              throw AppException.badRequest("Không xác định userId từ token");
            or = cb.or(or, cb.equal(root.get("creatorId"), currentUserId));
          }
          if (actualRoles.contains("DRIVER")) {
            if (currentUserId == null)
              throw AppException.badRequest("Không xác định driverId từ token");
            or = cb.or(or, cb.equal(root.get("shipperId"), currentUserId));
          }
          // if no role matched -> deny
          return or;
        };

    Specification<OrderEntity> spec = roleSpec;
    return spec;
  }

  // helper to AND common filters
  private Specification<OrderEntity> applyCommonFilters(
      Specification<OrderEntity> base,
      String q,
      String status,
      String paymentMethod,
      LocalDate fromDate,
      LocalDate toDate) {
    Specification<OrderEntity> spec = base;
    if (q != null && !q.isBlank()) spec = spec.and(OrderSpecifications.freeTextSearch(q));
    if (status != null && !status.isBlank()) spec = spec.and(OrderSpecifications.hasStatus(status));
    if (paymentMethod != null && !paymentMethod.isBlank())
      spec = spec.and(OrderSpecifications.hasPaymentMethod(paymentMethod));
    if (fromDate != null) spec = spec.and(OrderSpecifications.fromDate(fromDate));
    if (toDate != null) spec = spec.and(OrderSpecifications.toDate(toDate));
    return spec;
  }

  public OrderDetailResponse getOrderById(UUID currentUserId, Set<String> roleNames, UUID orderId) {
    OrderEntity entity =
        orderRepo.findById(orderId).orElseThrow(() -> AppException.badRequest("Order not found"));

    // If admin -> allow
    if (roleNames != null && (roleNames.contains("ADMIN") || roleNames.contains("DRIVER"))) {
      return OrderMapper.toDetail(entity);
    }

    // If driver -> allow if they are assigned shipper
    //    if (roleNames != null && roleNames.contains("DRIVER")) {
    //      UUID shipperId = entity.getShipperId();
    //      if (shipperId != null && shipperId.equals(currentUserId)) {
    //        return OrderMapper.toDetail(entity);
    //      }
    //    }

    // If regular user -> allow if creator
    if (roleNames != null && roleNames.contains("USER")) {
      if (entity.getCreatorId().equals(currentUserId)) {
        return OrderMapper.toDetail(entity);
      }
    }

    // If none matched -> access denied
    throw AppException.badRequest("Access denied");
  }

  @Transactional
  public OrderDetailResponse updateOrderStatus(
      UUID currentUserId, Set<String> roleNames, UUID orderId, OrderStatusUpdateRequest req) {

    if (req == null || req.newStatus() == null) {
      throw AppException.badRequest("newStatus is required");
    }

    OrderEntity order =
        orderRepo.findById(orderId).orElseThrow(() -> AppException.badRequest("Order not found"));

    OrderStatus from = order.getStatus();
    OrderStatus to = req.newStatus();

    // early no-op: nếu cùng status -> trả về luôn (hoặc bạn có thể ném lỗi)
    if (from == to) {
      return OrderMapper.toDetail(order);
    }

    boolean isAdmin = roleNames != null && roleNames.contains("ADMIN");
    boolean isDriver = roleNames != null && roleNames.contains("DRIVER");
    boolean isUser = roleNames != null && roleNames.contains("USER");

    // ---------- Authorization: kiểm tra permission (ANY role thỏa là ok) ----------
    boolean permitted = false;

    if (isAdmin) {
      permitted = true; // admin có quyền làm mọi thứ
    } else {
      // driver: phải là shipper được assign và status mới thuộc tập driver được phép
      if (isDriver && order.getShipperId() != null && order.getShipperId().equals(currentUserId)) {
        var allowedForDriver =
            Set.of(
                OrderStatus.DRIVER_EN_ROUTE_PICKUP,
                OrderStatus.ARRIVED_PICKUP,
                OrderStatus.PICKUP_ATTEMPT_FAILED,
                OrderStatus.PICKUP_FAILED,
                OrderStatus.PACKAGE_PICKED,
                OrderStatus.EN_ROUTE_DELIVERY,
                OrderStatus.ARRIVED_DELIVERY,
                OrderStatus.DELIVERY_ATTEMPT_FAILED,
                OrderStatus.DELIVERY_FAILED,
                OrderStatus.RETURNING_TO_SENDER,
                OrderStatus.RETURNED,
                OrderStatus.DELIVERED,
                OrderStatus.DRIVER_ISSUE_REPORTED,
                OrderStatus.REASSIGNING_DRIVER,
                OrderStatus.CANCELLED_BY_DRIVER);
        if (allowedForDriver.contains(to)) permitted = true;
      }

      // sender/user: chỉ cho cancel (CANCELLED_BY_SENDER) trong các trạng thái sơ bộ
      if (!permitted
          && isUser
          && order.getCreatorId() != null
          && order.getCreatorId().equals(currentUserId)) {
        var cancellableBySender =
            Set.of(
                OrderStatus.FINDING_DRIVER,
                OrderStatus.DRIVER_ASSIGNED,
                OrderStatus.DRIVER_EN_ROUTE_PICKUP,
                OrderStatus.ARRIVED_PICKUP);
        if (to == OrderStatus.CANCELLED_BY_SENDER && cancellableBySender.contains(from)) {
          permitted = true;
        }
      }
    }

    if (!permitted) {
      throw AppException.forbidden("Access denied or role not allowed to set status " + to);
    }

    // ---------- Business rules ----------
    UUID oldShipper = order.getShipperId();

    // 1) reassign-request by driver/admin: clear shipper if REASSIGNING_DRIVER
    if (to == OrderStatus.REASSIGNING_DRIVER) {
      order.setShipperId(null);
    }

    // 2) change shipper only allowed for admin via newShipperId
    if (req.newShipperId() != null) {
      if (!isAdmin) {
        throw AppException.forbidden("Only admin can change shipper directly");
      }
      // if newShipperId equals oldShipper -> no real change
      if (!req.newShipperId().equals(oldShipper)) {
        order.setShipperId(req.newShipperId());
      }
    }

    // 3) apply status change
    order.setStatus(to);

    // persist order (within transaction)
    orderRepo.save(order);

    // ---------- Save history ----------
    OrderStatusHistory hist = new OrderStatusHistory();
    hist.setOrderId(order.getId()); // simplified history stores orderId
    hist.setFromStatus(from);
    hist.setToStatus(to);
    hist.setChangedBy(currentUserId);
    hist.setReason(req.reasonNote()); // optional
    hist.setOldShipperId(oldShipper);
    hist.setNewShipperId(order.getShipperId());
    hist.setCreatedAt(LocalDateTime.now());
    historyRepo.save(hist);

//    // ---------- Prepare notifications (do NOT send inside transaction) ----------
//    // We'll collect what to send and perform send after commit via TransactionSynchronization
//    final UUID finalOldShipper = oldShipper;
//    final UUID finalNewShipper = order.getShipperId();
//    final OrderStatus finalFrom = from;
//    final OrderStatus finalTo = to;
//    final UUID creatorId = order.getCreatorId();
//    final UUID assignedShipper = order.getShipperId();
//    final UUID orderIdForNotify = order.getId();
//
//    // Build user message and driver message content depending on target status
//    // We'll prepare simple title/body and data map for FCM
//
//    NotifyPayload userPayload = null;
//    NotifyPayload driverPayload = null;
//
//    // --- Decide notifications for user ---
//    switch (finalTo) {
//      case REASSIGNING_DRIVER -> {
//        String title = "🔄 Đang tìm tài xế mới";
//        String body = "Đơn hàng của bạn đang được tìm tài xế mới, vui lòng chờ.";
//        userPayload =
//            new NotifyPayload(
//                title,
//                body,
//                buildDataMap("ORDER_REASSIGNING", orderIdForNotify, title, body, null));
//      }
//      case DRIVER_ASSIGNED -> {
//        if (assignedShipper != null) {
//          String title = "✅ Đã có tài xế nhận đơn";
//          String body = "Tài xế đang được liên hệ để tới lấy hàng.";
//          userPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap(
//                      "DRIVER_ASSIGNED",
//                      orderIdForNotify,
//                      title,
//                      body,
//                      assignedShipper.toString()));
//        }
//      }
//      case DRIVER_EN_ROUTE_PICKUP -> {
//        String title = "🚗 Tài xế đang tới điểm lấy";
//        String body = "Tài xế đang trên đường tới điểm lấy hàng.";
//        userPayload =
//            new NotifyPayload(
//                title,
//                body,
//                buildDataMap("DRIVER_EN_ROUTE_PICKUP", orderIdForNotify, title, body, null));
//      }
//      case ARRIVED_PICKUP -> {
//        String title = "📍 Tài xế đã tới điểm lấy";
//        String body = "Tài xế đã tới địa điểm lấy hàng.";
//        userPayload =
//            new NotifyPayload(
//                title, body, buildDataMap("ARRIVED_PICKUP", orderIdForNotify, title, body, null));
//      }
//      case PACKAGE_PICKED -> {
//        String title = "📦 Đã lấy hàng";
//        String body = "Tài xế đã lấy hàng và chuẩn bị giao.";
//        userPayload =
//            new NotifyPayload(
//                title, body, buildDataMap("PICKUP_SUCCESS", orderIdForNotify, title, body, null));
//      }
//      case EN_ROUTE_DELIVERY -> {
//        String title = "🚚 Đang giao hàng";
//        String body = "Tài xế đang di chuyển đến địa chỉ giao hàng.";
//        userPayload =
//            new NotifyPayload(
//                title,
//                body,
//                buildDataMap("EN_ROUTE_DELIVERY", orderIdForNotify, title, body, null));
//      }
//      case ARRIVED_DELIVERY -> {
//        String title = "📍 Đã đến nơi giao hàng";
//        String body = "Tài xế đã tới địa điểm giao hàng.";
//        userPayload =
//            new NotifyPayload(
//                title, body, buildDataMap("ARRIVED_DELIVERY", orderIdForNotify, title, body, null));
//      }
//      case DELIVERED -> {
//        String title = "🎉 Giao hàng thành công";
//        String body = "Đơn hàng đã được giao thành công. Cảm ơn bạn!";
//        userPayload =
//            new NotifyPayload(
//                title, body, buildDataMap("DELIVERED", orderIdForNotify, title, body, null));
//      }
//      case CANCELLED_BY_DRIVER, CANCELLED_BY_SENDER, CANCELLED_NO_DRIVER, ORDER_CANCELLED -> {
//        String title = "❌ Đơn hàng bị hủy";
//        String body = "Đơn hàng của bạn đã bị hủy. Vui lòng kiểm tra chi tiết.";
//        userPayload =
//            new NotifyPayload(
//                title, body, buildDataMap("ORDER_CANCELLED", orderIdForNotify, title, body, null));
//      }
//      default -> {
//        // other transitions: no notification to user by default
//      }
//    }
//
//    // --- Decide notifications for driver (assigned shipper only) ---
//    if (assignedShipper != null) {
//      String assignedShipperId = assignedShipper.toString();
//      switch (finalTo) {
//        case DRIVER_ASSIGNED -> {
//          String title = "📦 Bạn được giao một đơn hàng";
//          String body = "Bạn vừa được gán đơn, vui lòng vào app xem và xác nhận.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap("ASSIGNED_ORDER", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case DRIVER_EN_ROUTE_PICKUP -> {
//          String title = "🚗 Đến điểm lấy";
//          String body = "Vui lòng di chuyển tới điểm lấy hàng.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap(
//                      "EN_ROUTE_PICKUP", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case ARRIVED_PICKUP -> {
//          String title = "📍 Đã đến điểm lấy";
//          String body = "Bạn đã đến điểm lấy. Vui lòng liên hệ người gửi nếu cần.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap("ARRIVED_PICKUP", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case PACKAGE_PICKED -> {
//          String title = "📦 Đã lấy hàng";
//          String body = "Bạn đã xác nhận lấy hàng. Hãy chuyển sang giao hàng.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap(
//                      "PICKUP_CONFIRMED", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case EN_ROUTE_DELIVERY -> {
//          String title = "🚚 Đang giao";
//          String body = "Vui lòng giao hàng tới địa chỉ người nhận.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap(
//                      "EN_ROUTE_DELIVERY", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case DELIVERED -> {
//          String title = "✅ Giao hàng xong";
//          String body = "Bạn đã hoàn thành giao hàng. Cảm ơn!";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap("DELIVERED", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case REASSIGNING_DRIVER -> {
//          String title = "🔄 Đang reassign";
//          String body = "Đơn hàng này đang được tìm tài xế mới. Vui lòng chờ.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap("REASSIGNING", orderIdForNotify, title, body, assignedShipperId));
//        }
//        case CANCELLED_BY_DRIVER, CANCELLED_BY_SENDER, ORDER_CANCELLED -> {
//          String title = "❌ Đơn hàng bị hủy";
//          String body = "Đơn hàng đã bị hủy. Không cần thực hiện nhiệm vụ này nữa.";
//          driverPayload =
//              new NotifyPayload(
//                  title,
//                  body,
//                  buildDataMap(
//                      "ORDER_CANCELLED", orderIdForNotify, title, body, assignedShipperId));
//        }
//        default -> {
//          // other transitions: no notification to driver by default
//        }
//      }
//    }
//
//    // ---------- Register afterCommit to actually send notifications ----------
//    NotifyPayload finalUserPayload = userPayload;
//    NotifyPayload finalDriverPayload = driverPayload;
//    TransactionSynchronizationManager.registerSynchronization(
//        new TransactionSynchronization() {
//          @Override
//          public void afterCommit() {
//            // 1) notify user if needed
//            if (finalUserPayload != null && creatorId != null) {
//              try {
//                String userToken =
//                    redisTemplate.opsForValue().get(String.format(USER_FCM_TOKEN, creatorId));
//                if (userToken != null && !userToken.isEmpty()) {
//                  firebaseMessagingService.sendNotificationWithData(
//                      userToken,
//                      finalUserPayload.title,
//                      finalUserPayload.body,
//                      finalUserPayload.data);
//                }
//              } catch (Exception e) {
//                // log error but do not throw
//              }
//            }
//
//            // 2) notify assigned driver if needed
//            if (finalDriverPayload != null && assignedShipper != null) {
//              try {
//                String driverToken =
//                    redisTemplate
//                        .opsForValue()
//                        .get(String.format(DRIVER_FCM_TOKEN, assignedShipper));
//                if (driverToken != null && !driverToken.isEmpty()) {
//                  firebaseMessagingService.sendNotificationWithData(
//                      driverToken,
//                      finalDriverPayload.title,
//                      finalDriverPayload.body,
//                      finalDriverPayload.data);
//                }
//              } catch (Exception e) {
//                // log error
//              }
//            }
//
//            // 3) If status is DRIVER_ASSIGNED but assignedShipper == null (system assigned via
//            // push),
//            // you might want to trigger push-service to start broadcasting to top-k drivers.
//            // That logic is outside this method (handled by OrderCreatedEvent or Reassign flow).
//          }
//        });
//
//    // ---------- Optional: publish domain event (non-blocking) ----------
//    // eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), from, to, currentUserId));

    return OrderMapper.toDetail(order);
  }

  // --- Decide notifications for user ---
  // helper nhỏ để build map data (status=title, message=body)
  private Map<String, String> buildDataMap(
      String type, UUID orderId, String title, String body, String driverId) {
    Map<String, String> data = new HashMap<>();
    data.put("type", type);
    data.put("orderId", orderId.toString());
    if (driverId != null) data.put("driverId", driverId);
    // thêm theo yêu cầu: status = title, message = body
    data.put("status", title);
    data.put("message", body);
    return data;
  }

  public List<OrderStatusHistoryResponse> getOrderHistory(
      UUID currentUserId, Set<String> roles, UUID orderId) {
    // Check order existence
    OrderEntity order =
        orderRepo.findById(orderId).orElseThrow(() -> AppException.badRequest("Order not found"));

    // Authorization
    boolean isAdmin = roles != null && roles.contains("ADMIN");
    boolean isDriver = roles != null && roles.contains("DRIVER");
    boolean isUser = roles != null && roles.contains("USER");

    if (!(isAdmin
        || (isDriver && order.getShipperId() != null && order.getShipperId().equals(currentUserId))
        || (isUser && order.getCreatorId().equals(currentUserId)))) {
      throw AppException.forbidden("Access denied");
    }

    List<OrderStatusHistory> histories = historyRepo.findAllByOrderIdOrderByCreatedAtAsc(orderId);

    return histories.stream().map(OrderMapper::toHistory).toList();
  }

  public List<PriceAndRouteDto> computePriceAndRouteForOrder(CreateOrderRequest order) {
    StringBuilder origins =
        new StringBuilder(
            order.pickupAddress().latitude() + "," + order.pickupAddress().longitude());
    Map<Integer, PackageDto> indexToPackage = new HashMap<>();
    int index = 1;
    double curWeight = 0;
    for (PackageDto pkg : order.packages()) {
      String location = pkg.receiverAddress().latitude() + "," + pkg.receiverAddress().longitude();
      origins.append("|").append(location);
      indexToPackage.put(index, pkg);
      curWeight += pkg.weightKg();
      index++;
    }

    try {
      String vehicle = "bike";
      DistanceMatrixRequest request =
          new DistanceMatrixRequest(
              origins.toString(), origins.toString(), vehicle); // destinations = origins
      Mono<DistanceMatrixResponse> response = gooongMapClient.getDistanceMatrixWithQuery(request);
      if (response == null) {
        throw AppException.badRequest("Gooong Map API returned null response");
      }
      DistanceMatrixResponse data = response.block();
      if (data == null) {
        throw AppException.badRequest("Gooong Map API returned null data");
      }
      // distance
      int[][] weightMatrix = toWeightMatrix(data, true);
      int[][] durationMatrix = toWeightMatrix(data, false);
      List<Integer> route = computeRoute(weightMatrix);
      ShippingConfig cfg =
          shippingConfigRepo
              .findById(1L)
              .orElseThrow(() -> new IllegalStateException("Shipping config (id=1) not found"));
      List<PriceAndRouteDto> routeDtos = new ArrayList<>();
      double curPrice = 0;
      int distance = 0;
      int estimatedDuration = 0;
      int totalDestinations = route.size() - 1;
      for (int i = 1; i < route.size(); i++) {
        int fromIdx = route.get(i - 1);
        int toIdx = route.get(i);
        int weight = weightMatrix[fromIdx][toIdx];
        int duration = durationMatrix[fromIdx][toIdx];
        double distanceKm = weight / 1000.0;
        distance += weight;
        estimatedDuration += duration;
        if (toIdx == 0) {
          // returning to origin, skip
          continue;
        }

        PackageDto pkg = indexToPackage.get(toIdx);
        if (pkg == null) {
          throw AppException.badRequest("Package not found for index: " + toIdx);
        }

        curPrice +=
            calculateTotalShippingCost(distanceKm, curWeight, pkg.size(), cfg) / totalDestinations;

        PriceAndRouteDto dto =
            new PriceAndRouteDto(
                Math.max(cfg.getMinFee(), Math.round(curPrice / 1000) * 1000),
                pkg.receiverAddress().latitude(),
                pkg.receiverAddress().longitude(),
                i,
                toIdx,
                distance,
                estimatedDuration);
        totalDestinations--;
        curWeight -= pkg.weightKg();
        routeDtos.add(dto);
      }
      return routeDtos;
    } catch (Exception e) {
      throw AppException.badRequest("Failed to compute route: " + e.getMessage());
    }
  }

  public int[][] toWeightMatrix(DistanceMatrixResponse resp, boolean forDistance) {
    if (resp == null || resp.rows() == null) {
      throw new IllegalArgumentException("DistanceMatrixResponse or rows is null");
    }
    int rowsCount = resp.rows().size();
    if (rowsCount == 0) return new int[0][0];

    int colsCount = resp.rows().getFirst().elements().size();
    int[][] matrix = new int[rowsCount][colsCount];

    for (int i = 0; i < rowsCount; i++) {
      DistanceMatrixResponse.Row row = resp.rows().get(i);
      if (row == null || row.elements() == null) {
        throw new IllegalArgumentException("Row " + i + " is null or has no elements");
      }
      if (row.elements().size() != colsCount) {
        throw new IllegalArgumentException("Inconsistent number of elements in row " + i);
      }

      for (int j = 0; j < colsCount; j++) {
        DistanceMatrixResponse.Element el = row.elements().get(j);
        if (el.status() == null || !el.status().equals("OK")) {
          throw new IllegalArgumentException(
              "Element at (" + i + "," + j + ") has invalid status: " + el.status());
        }
        if (forDistance) {
          matrix[i][j] = el.distance().value();
        } else {
          matrix[i][j] = el.duration().value();
        }
      }
    }
    return matrix;
  }

  public List<Integer> computeRoute(int[][] weightMatrix) {
    int n = weightMatrix.length; // including point A at index 0
    int N = n - 1; // number of delivery points
    int[][] dp = new int[1 << N][n];
    int[][] parent = new int[1 << N][n];

    // Init: from 0 to each point i
    for (int i = 1; i < n; i++) {
      int mask = 1 << (i - 1); // bit i-1 represents point i
      dp[mask][i] = weightMatrix[0][i];
      parent[mask][i] = 0;
    }

    // DP
    for (int mask = 1; mask < (1 << N); mask++) {
      for (int u = 1; u < n; u++) {
        if ((mask & (1 << (u - 1))) == 0) continue;
        int prevMask = mask ^ (1 << (u - 1));
        if (prevMask == 0) continue;

        dp[mask][u] = Integer.MAX_VALUE / 2;
        for (int v = 1; v < n; v++) {
          if ((prevMask & (1 << (v - 1))) == 0) continue;
          int cost = dp[prevMask][v] + weightMatrix[v][u];
          if (cost < dp[mask][u]) {
            dp[mask][u] = cost;
            parent[mask][u] = v;
          }
        }
      }
    }

    // Find best end point
    int finalMask = (1 << N) - 1;
    int end = -1;
    int minCost = Integer.MAX_VALUE / 2;
    for (int u = 1; u < n; u++) {
      if (dp[finalMask][u] < minCost) {
        minCost = dp[finalMask][u];
        end = u;
      }
    }

    // Reconstruct path
    List<Integer> path = new ArrayList<>();
    int mask = finalMask;
    while (end != 0) {
      path.add(end);
      int temp = parent[mask][end];
      mask ^= (1 << (end - 1));
      end = temp;
    }
    path.add(0);
    Collections.reverse(path);
    return path;
  }

  public double calculateTotalShippingCost(
      double distanceKm, double actualWeightKg, PackageSize size, ShippingConfig cfg) {
    SizeConfig sizeCfg =
        sizeConfigRepo
            .findBySizeCode(size)
            .orElseThrow(
                () -> new IllegalStateException("Size config not found for size: " + size));
    // 1) distance fee
    double extraKm = Math.max(0, distanceKm - cfg.getBaseKm());
    long distanceFee = Math.round(cfg.getBaseKmFee() + extraKm * cfg.getRatePerKm());

    // 2) size surcharge
    long sizeFee = sizeCfg.getSurcharge();

    // 3) weight fee
    long weightFee = Math.round(actualWeightKg * cfg.getRatePerKg());

    // 4) subtotal
    double subtotal = distanceFee + sizeFee + weightFee;

    // 5) fuel surcharge
    if (cfg.getFuelSurchargePercent() != null && cfg.getFuelSurchargePercent() > 0) {
      subtotal = subtotal * (1 + cfg.getFuelSurchargePercent() / 100.0);
    }

    //    return Math.max(cfg.getMinFee(), Math.round(subtotal));
    return subtotal;
  }

  public OrderDetailResponse assignDriverToOrder(UUID driverId, UUID orderId) {
    OrderEntity order =
        orderRepo.findById(orderId).orElseThrow(() -> AppException.badRequest("Order not found"));

    if (order.getShipperId() != null) {
      throw AppException.badRequest("Order already has a driver assigned");
    }
    order.setShipperId(driverId);
    order.setStatus(OrderStatus.DRIVER_ASSIGNED);
    orderRepo.save(order);
    return OrderMapper.toDetail(order);
  }
}
