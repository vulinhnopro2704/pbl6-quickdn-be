package com.pbl6.order.service;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.*;
import com.pbl6.order.exception.AppException;
import com.pbl6.order.mapper.OrderMapper;
import com.pbl6.order.repository.OrderRepository;
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

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest req) {
        // get creator id or phone from security context (subject)
        String creatorSubject = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID creatorId = null;
        try {
            creatorId = UUID.fromString(creatorSubject);
        } catch (Exception ignored) {}

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

        // temp assign shipper: dùng UUID hợp lệ. Nếu muốn null, bỏ dòng này.
        order.setShipperId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        if (req.scheduledAt() != null) {
            order.setScheduledAt(LocalDateTime.parse(req.scheduledAt()));
        }

        // SAVE order (packages sẽ được persist bởi cascade = CascadeType.ALL)
        orderRepo.save(order);

        // trả về totalAmount (BigDecimal). CreateOrderResponse now uses OrderStatus enum for status
        return new CreateOrderResponse(order.getId(), order.getTotalAmount().doubleValue(), "VND", order.getStatus());
    }

    private PackageAddressEntity createAddress(AddressDto addrDto) {
        PackageAddressEntity addr = new PackageAddressEntity();
        addr.setDetail(addrDto.detail());
        addr.setName(addrDto.name());
        addr.setPhone(addrDto.phone());

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

    // --- existing simple list method (kept for compatibility) ---
    public OrderListResponse getMyOrders(UUID userId) {
        var list = orderRepo.findByCreatorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderMapper::toListItem)
                .toList();
        return new OrderListResponse(list);
    }

    // --- New paged/search/filter method (does NOT use Specification.where(...)) ---
    public org.springframework.data.domain.Page<OrderListItemResponse> getMyOrders(
            UUID userId,
            String q,
            String status,
            String paymentMethod,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            Pageable pageable
    ) {
        // Start with required condition: belongsToUser
        Specification<OrderEntity> spec = OrderSpecifications.belongsToUser(userId);

        if (q != null && !q.isBlank()) {
            spec = spec.and(OrderSpecifications.freeTextSearch(q));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and(OrderSpecifications.hasStatus(status));
        }
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            spec = spec.and(OrderSpecifications.hasPaymentMethod(paymentMethod));
        }
        if (fromDate != null) {
            spec = spec.and(OrderSpecifications.fromDate(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(OrderSpecifications.toDate(toDate));
        }

        var page = orderRepo.findAll(spec, pageable);
        return page.map(OrderMapper::toListItem);
    }

    public Page<OrderListItemResponse> getOrdersByRoleSelector(
            UUID currentUserId,
            Set<String> actualRoles,
            String roleToUse,         // e.g. "ROLE_USER", "ROLE_DRIVER", "ROLE_ADMIN", or null (auto)
            UUID filterUserId,        // only meaningful if roleToUse == ROLE_ADMIN
            String q, String status, String paymentMethod,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        // If roleToUse is explicitly admin, ensure actualRoles contains it (controller checked already)
        if ("ROLE_ADMIN".equals(roleToUse)) {
            Specification<OrderEntity> spec = (root, query, cb) -> null;
            if (filterUserId != null) spec = spec.and(OrderSpecifications.belongsToUser(filterUserId));
            // add common filters
            spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
            return orderRepo.findAll(spec, pageable).map(OrderMapper::toListItem);
        }

        // If roleToUse is explicitly DRIVER
        if ("ROLE_DRIVER".equals(roleToUse)) {
            if (currentUserId == null) throw AppException.badRequest("Không xác định driverId từ token");
            Specification<OrderEntity> spec = OrderSpecifications.hasShipperId(currentUserId);
            spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
            return orderRepo.findAll(spec, pageable).map(OrderMapper::toListItem);
        }

        // If roleToUse is explicitly USER
        if ("ROLE_USER".equals(roleToUse)) {
            if (currentUserId == null) throw AppException.badRequest("Không xác định userId từ token");
            Specification<OrderEntity> spec = OrderSpecifications.belongsToUser(currentUserId);
            spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
            return orderRepo.findAll(spec, pageable).map(OrderMapper::toListItem);
        }

        // roleToUse == null => auto / union behavior based on actualRoles
        // Build OR predicates for roles the user actually has (e.g. USER and/or DRIVER)
        Specification<OrderEntity> spec = getOrderEntitySpecification(currentUserId, actualRoles);
        spec = applyCommonFilters(spec, q, status, paymentMethod, fromDate, toDate);
        return orderRepo.findAll(spec, pageable).map(OrderMapper::toListItem);
    }

    private static Specification<OrderEntity> getOrderEntitySpecification(UUID currentUserId, Set<String> actualRoles) {
        Specification<OrderEntity> roleSpec = (root, query, cb) -> {
            Predicate or = cb.disjunction();
            if (actualRoles.contains("ROLE_USER")) {
                if (currentUserId == null) throw AppException.badRequest("Không xác định userId từ token");
                or = cb.or(or, cb.equal(root.get("creatorId"), currentUserId));
            }
            if (actualRoles.contains("ROLE_DRIVER")) {
                if (currentUserId == null) throw AppException.badRequest("Không xác định driverId từ token");
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
            String q, String status, String paymentMethod, LocalDate fromDate, LocalDate toDate
    ) {
        Specification<OrderEntity> spec = base;
        if (q != null && !q.isBlank()) spec = spec.and(OrderSpecifications.freeTextSearch(q));
        if (status != null && !status.isBlank()) spec = spec.and(OrderSpecifications.hasStatus(status));
        if (paymentMethod != null && !paymentMethod.isBlank()) spec = spec.and(OrderSpecifications.hasPaymentMethod(paymentMethod));
        if (fromDate != null) spec = spec.and(OrderSpecifications.fromDate(fromDate));
        if (toDate != null) spec = spec.and(OrderSpecifications.toDate(toDate));
        return spec;
    }

    public OrderDetailResponse getOrderById(UUID currentUserId, Set<String> roleNames, UUID orderId) {
        OrderEntity entity = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.badRequest("Order not found"));

        // If admin -> allow
        if (roleNames != null && roleNames.contains("ROLE_ADMIN")) {
            return OrderMapper.toDetail(entity);
        }

        // If driver -> allow if they are assigned shipper
        if (roleNames != null && roleNames.contains("ROLE_DRIVER")) {
            UUID shipperId = entity.getShipperId();
            if (shipperId != null && shipperId.equals(currentUserId)) {
                return OrderMapper.toDetail(entity);
            }
        }

        // If regular user -> allow if creator
        if (roleNames != null && roleNames.contains("ROLE_USER")) {
            if (entity.getCreatorId().equals(currentUserId)) {
                return OrderMapper.toDetail(entity);
            }
        }

        // If none matched -> access denied
        throw AppException.badRequest("Access denied");
    }


}
