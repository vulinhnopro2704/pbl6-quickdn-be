package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistory {
  @Id @GeneratedValue private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status")
  private OrderStatus fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", nullable = false)
  private OrderStatus toStatus;

  @Column(name = "changed_by")
  private UUID changedBy;

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @Column(name = "old_shipper_id")
  private UUID oldShipperId;

  @Column(name = "new_shipper_id")
  private UUID newShipperId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
