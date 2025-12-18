package com.pbl6.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Store webhook events and payOS API responses for audit trail */
@Entity
@Table(
    name = "payment_events",
    indexes = {
      @Index(name = "idx_payment_events_payment_id", columnList = "paymentId"),
      @Index(name = "idx_payment_events_order_code", columnList = "orderCode")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Reference to Payment.id (no FK on purpose, async-safe) */
  @Column(name = "payment_id")
  private Long paymentId;

  /** Business order code for fast lookup */
  @Column(name = "order_code")
  private Long orderCode;

  /** WEBHOOK, CREATE_RESPONSE, GET_RESPONSE, CANCEL_RESPONSE */
  @Column(name = "event_type", nullable = false, length = 50)
  private String eventType;

  /** Raw JSON payload */
  @Column(columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
