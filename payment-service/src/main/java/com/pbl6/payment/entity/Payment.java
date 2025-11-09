package com.pbl6.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payment entity for storing payment information in local DB
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_code", columnList = "orderCode", unique = true),
    @Index(name = "idx_payos_payment_link_id", columnList = "payosPaymentLinkId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long orderCode;
    
    @Column(nullable = false)
    private Long amount; // VND
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    
    @Column(length = 255)
    private String payosPaymentLinkId;
    
    @Column(length = 500)
    private String checkoutUrl;
    
    @Column
    private Long expiredAt; // Unix timestamp
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String meta; // JSON string for additional data
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
