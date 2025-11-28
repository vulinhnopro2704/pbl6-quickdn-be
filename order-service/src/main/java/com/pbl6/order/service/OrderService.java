package com.pbl6.order.service;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.*;
import com.pbl6.order.exception.AppException;
import com.pbl6.order.mapper.OrderMapper;
import com.pbl6.order.repository.OrderRepository;
import com.pbl6.order.repository.OrderStatusHistoryRepository;
import com.pbl6.order.repository.PackageAddressRepository;
import com.pbl6.order.repository.PackageRepository;
import com.pbl6.order.spec.OrderSpecifications;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepo;
  private final PackageRepository packageRepo;
  private final PackageAddressRepository addressRepo;
  private final PricingService pricingService;
  private final OrderStatusHistoryRepository historyRepo;

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

    // ví dụ giá/ước lượng tạm thời (BigDecimal)
    BigDecimal sampleValue = BigDecimal.valueOf(36.0);

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

      // estimate distance/duration/fee (hiện là giá cố định sampleValue)
      // nếu bạn có pricingService, gọi nó để có giá chính xác thay vì dùng sampleValue
      BigDecimal deliveryFee = sampleValue; // ví dụ tạm
      pkg.setDeliveryFee(deliveryFee);
      pkg.setEstimatedDistanceKm(sampleValue);
      pkg.setEstimatedDurationMin(36);

      // cộng dồn vào tổng (BigDecimal)
      estimatedDistanceKm = estimatedDistanceKm.add(sampleValue);
      estimatedDurationMin += 36;
      totalAmount = totalAmount.add(deliveryFee);

      // lưu package sẽ được cascade khi save order; nếu bạn muốn lưu riêng vẫn có thể
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

    order.setPackages(packageEntities);
    order.setEstimatedDistanceKm(estimatedDistanceKm);
    order.setEstimatedDurationMin(estimatedDurationMin);
    order.setTotalAmount(totalAmount);

    // temp assign shipper
    order.setShipperId(UUID.fromString("867c8938-37e3-4fa8-9805-2bccb70104f7"));

    if (req.scheduledAt() != null) {
      order.setScheduledAt(LocalDateTime.parse(req.scheduledAt()));
    }

    // SAVE order (packages sẽ được persist bởi cascade = CascadeType.ALL)
    orderRepo.save(order);

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
    if (roleNames != null && roleNames.contains("ADMIN")) {
      return OrderMapper.toDetail(entity);
    }

    // If driver -> allow if they are assigned shipper
    if (roleNames != null && roleNames.contains("DRIVER")) {
      UUID shipperId = entity.getShipperId();
      if (shipperId != null && shipperId.equals(currentUserId)) {
        return OrderMapper.toDetail(entity);
      }
    }

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
      // if admin sets REASSIGNING_DRIVER and also provides newShipperId later, admin can reassign
      // too.
      order.setShipperId(null);
      // finding new driver in another thread/process is out of scope here

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

    // ---------- Optional: publish domain event ----------
    // eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), from, to, currentUserId));

    return OrderMapper.toDetail(order);
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
}
