package com.pbl6.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Payment entity for storing payment information in local DB */
@Entity
@Table(
    name = "payment",
    indexes = {
      @Index(name = "idx_payment_order_code", columnList = "orderCode"),
      @Index(name = "idx_payment_payos_payment_link_id", columnList = "payosPaymentLinkId")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Business order code (unique across system) */
  @Column(name = "order_code", nullable = false, unique = true)
  private Long orderCode;

  /** Amount in VND */
  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false, length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  /** payOS payment link id */
  @Column(name = "payos_payment_link_id", length = 255)
  private String payosPaymentLinkId;

  @Column(name = "checkout_url", length = 500)
  private String checkoutUrl;

  /** Expired time (unix timestamp, seconds) */
  @Column(name = "expired_at")
  private Long expiredAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /** Extra metadata in JSON format */
  @Column(columnDefinition = "TEXT")
  private String meta;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
