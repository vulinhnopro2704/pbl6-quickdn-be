package com.pbl6.order.dto;

import com.pbl6.order.dto.payment.PaymentResponse;
import com.pbl6.order.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        AddressDto pickupAddress,
        List<PackageItemResponse> packages,
        UUID shipperId,
        List<PriceAndRouteDto> priceAndRoutes,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Long orderCode) {

  public record PackageItemResponse(
      UUID id,
      BigDecimal weightKg,
      PackageSize packageSize,
      BigDecimal deliveryFee,
      BigDecimal codFee,
      PayerType payerType,
      PackageCategory category,
      String imageUrl,
      PackageStatus packageStatus,
      String statusNote,
      LocalDateTime statusUpdatedAt,
      String description,
      AddressDto dropoffAddress) {}
}
