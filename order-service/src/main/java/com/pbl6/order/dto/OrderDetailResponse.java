package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;
import com.pbl6.order.entity.PayerType;
import com.pbl6.order.entity.PackageCategory;
import com.pbl6.order.entity.PackageSize;

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
    UUID shipperId) {

  public record PackageItemResponse(
      UUID id,
      BigDecimal weightKg,
      PackageSize packageSize,
      BigDecimal deliveryFee,
      BigDecimal codFee,
      PayerType payerType,
      PackageCategory category,
      String imageUrl,
      String description,
      AddressDto dropoffAddress) {}
}
