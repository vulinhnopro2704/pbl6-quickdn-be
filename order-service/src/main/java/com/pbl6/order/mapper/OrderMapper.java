package com.pbl6.order.mapper;

import com.pbl6.order.dto.AddressDto;
import com.pbl6.order.dto.OrderDetailResponse;
import com.pbl6.order.dto.OrderListItemResponse;
import com.pbl6.order.dto.OrderStatusHistoryResponse;
import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.entity.OrderStatusHistory;
import com.pbl6.order.entity.PackageAddressEntity;
import com.pbl6.order.entity.PackageEntity;

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
        a.getLatitude() != null ? a.getLatitude().doubleValue() : null,
        a.getLongitude() != null ? a.getLongitude().doubleValue() : null);
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
        e.getShipperId());
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
