package com.pbl6.order.mapper;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OrderMapper {

  public static OrderListItemResponse toListItem(OrderEntity e) {
    return new OrderListItemResponse(
        e.getId(), e.getTotalAmount(), e.getStatus(), e.getCreatedAt(), e.getScheduledAt());
  }

  public static AddressDto toAddress(PackageAddressEntity a) {
    if (a == null) return null;
    return new AddressDto(
        a.getDetail(),
        a.getName(),
        a.getPhone(),
        a.getWardCode(),
        a.getDistrictCode(),
        a.getNote(),
        a.getLatitude() != null ? a.getLatitude().doubleValue() : null,
        a.getLongitude() != null ? a.getLongitude().doubleValue() : null);
  }

  public static List<PriceAndRouteDto> toPriceAndRouteList(List<OrderPriceRouteEntity> prs) {
    if (prs == null || prs.isEmpty()) return List.of();
    return prs.stream()
        .filter(Objects::nonNull)
        .map(
            p ->
                new PriceAndRouteDto(
                    p.getPrice() != null ? p.getPrice().doubleValue() : null,
                    p.getLatitude() != null ? p.getLatitude().doubleValue() : null,
                    p.getLongitude() != null ? p.getLongitude().doubleValue() : null,
                    p.getRouteIndex(),
                    p.getPackageIndex(),
                    p.getDistance() != null ? p.getDistance() : null,
                    p.getEstimatedDuration() != null ? p.getEstimatedDuration() : null))
        .collect(Collectors.toList());
  }

  public static OrderDetailResponse.PackageItemResponse toPackageItem(PackageEntity p) {
    return new OrderDetailResponse.PackageItemResponse(
        p.getId(),
        p.getWeightKg(),
        p.getPackageSize(),
        p.getDeliveryFee(),
        p.getCodFee(),
        p.getPayerType(),
        p.getCategory(),
        p.getImageUrl(),
        p.getStatus(),
        p.getStatusNote(),
        p.getStatusUpdatedAt(),
        p.getDescription(),
        toAddress(p.getDropoffAddress()));
  }

  /**
   * Default toDetail (uses e.getPriceRoutes()) — safe because service attaches priceRoutes
   * beforehand.
   */
  public static OrderDetailResponse toDetail(OrderEntity e) {
    return new OrderDetailResponse(
        e.getId(),
        e.getTotalAmount(),
        e.getStatus(),
        e.getCreatedAt(),
        e.getScheduledAt(),
        toAddress(e.getPickupAddress()),
        e.getPackages().stream().map(OrderMapper::toPackageItem).toList(),
        e.getShipperId(),
        toPriceAndRouteList(e.getPriceRoutes()),
        e.getPaymentStatus(),
        e.getPaymentMethod(),
        e.getOrderCode());
  }

  public static OrderStatusHistoryResponse toHistory(OrderStatusHistory h) {
    return new OrderStatusHistoryResponse(
        h.getId(),
        h.getFromStatus(),
        h.getToStatus(),
        h.getChangedBy(),
        h.getReason(),
        h.getOldShipperId(),
        h.getNewShipperId(),
        h.getCreatedAt());
  }

  public static ReviewResponse toReviewResponse(OrderReviewEntity e) {
    if (e == null) return null;
    return new ReviewResponse(
        e.getId(),
        e.getOrderId(),
        e.getReviewerId(),
        e.getShipperId(),
        e.getRating(),
        e.getComment(),
        e.getCreatedAt());
  }
}
