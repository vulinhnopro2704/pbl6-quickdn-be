package com.pbl6.order.service;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.*;
import com.pbl6.order.repository.OrderRepository;
import com.pbl6.order.repository.PackageAddressRepository;
import com.pbl6.order.repository.PackageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            order.setScheduledAt(LocalDateTime.parse(req.scheduledAt()));
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
            pkg.setCategory(packageDto.category());
            if (packageDto.cod() != null && packageDto.cod()) {
                if (packageDto.codAmount() != null) {
                    pkg.setCodFee(BigDecimal.valueOf(packageDto.codAmount()));
                } else {
                    pkg.setCodFee(BigDecimal.ZERO);
                }
            } else {
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

}
