package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
public class PackageEntity {
  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dropoff_address_id", nullable = false)
  private PackageAddressEntity dropoffAddress;

  @Column(name = "weight_kg", precision = 8, scale = 3, nullable = false)
  private BigDecimal weightKg = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "package_size", nullable = false)
  private PackageSize packageSize;

  @Column(name = "delivery_fee", precision = 12, scale = 2, nullable = false)
  private BigDecimal deliveryFee = BigDecimal.ZERO;

  @Column(name = "cod_fee", precision = 12, scale = 2)
  private BigDecimal codFee = BigDecimal.ZERO;

  @Column(name = "estimated_distance_km", precision = 10, scale = 3)
  private BigDecimal estimatedDistanceKm = BigDecimal.ZERO;

  @Column(name = "estimated_duration_min")
  private Integer estimatedDurationMin = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "payer_type", nullable = false)
  private PayerType payerType = PayerType.SENDER;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private PackageCategory category = PackageCategory.DEFAULT;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
