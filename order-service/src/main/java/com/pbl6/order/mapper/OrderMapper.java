package com.pbl6.order.mapper;

import com.pbl6.order.dto.*;
import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.entity.OrderStatusHistory;
import com.pbl6.order.entity.PackageAddressEntity;
import com.pbl6.order.entity.PackageEntity;

import java.math.BigDecimal;
import java.util.List;

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

  public static List<PriceAndRouteDto> toPriceAndRoute(OrderEntity o) {
    return o.getPriceRoutes().stream()
        .map(
            p ->
                new PriceAndRouteDto(
                    p.getPrice().doubleValue(),
                    p.getLatitude().doubleValue(),
                    p.getLongitude().doubleValue(),
                    p.getRouteIndex(),
                    p.getPackageIndex(),
                    p.getDistance(),
                    p.getEstimatedDuration()))
        .toList();
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
        toPriceAndRoute(e));
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
}
