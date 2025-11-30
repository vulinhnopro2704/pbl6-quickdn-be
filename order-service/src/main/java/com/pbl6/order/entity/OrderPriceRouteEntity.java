package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_price_routes")
@Getter
@Setter
@NoArgsConstructor
public class OrderPriceRouteEntity {
  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Column(name = "price", precision = 12, scale = 2, nullable = false)
  private BigDecimal price;

  @Column(name = "latitude", precision = 11, scale = 7)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 11, scale = 7)
  private BigDecimal longitude;

  @Column(name = "route_index")
  private Integer routeIndex;

  @Column(name = "package_index")
  private Integer packageIndex;

  // distance: lưu mét (int) hoặc km (BigDecimal) tùy bạn; dùng int (m) ở ví dụ này
  @Column(name = "distance_m")
  private Integer distance;

  @Column(name = "estimate_duration_s")
  private Integer estimatedDuration; // seconds

  // constructor tiện lợi
  public OrderPriceRouteEntity(
      OrderEntity order,
      BigDecimal price,
      BigDecimal latitude,
      BigDecimal longitude,
      Integer routeIndex,
      Integer packageIndex,
      Integer distance,
      Integer estimatedDuration) {
    this.order = order;
    this.price = price;
    this.latitude = latitude;
    this.longitude = longitude;
    this.routeIndex = routeIndex;
    this.packageIndex = packageIndex;
    this.distance = distance;
    this.estimatedDuration = estimatedDuration;
  }
}
