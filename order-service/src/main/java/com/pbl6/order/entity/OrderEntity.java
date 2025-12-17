package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
public class OrderEntity {
  @Id @GeneratedValue private UUID id;

  @Column(name = "order_code", nullable = false, unique = true)
  private Long orderCode;

  @Column(name = "creator_id", nullable = false)
  private UUID creatorId;

  @Column(name = "shipper_id")
  private UUID shipperId;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod = PaymentMethod.CASH; // default CASH

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  // dùng BigDecimal cho tiền
  @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickup_address_id")
  private PackageAddressEntity pickupAddress;

  @Column(name = "customer_note", columnDefinition = "text")
  private String customerNote;

  // dùng BigDecimal cho khoảng cách nếu muốn precision cố định
  @Column(name = "estimated_distance_km", precision = 10, scale = 3)
  private BigDecimal estimatedDistanceKm = BigDecimal.ZERO;

  @Column(name = "estimated_duration_min") // total
  private Integer estimatedDurationMin = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status = OrderStatus.FINDING_DRIVER;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PackageEntity> packages = new ArrayList<>();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderPriceRouteEntity> priceRoutes = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
