package com.pbl6.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Store webhook responses and payOS API responses for audit trail
 */
@Entity
@Table(name = "payment_events", indexes = {
    @Index(name = "idx_payment_id", columnList = "paymentId"),
    @Index(name = "idx_order_code", columnList = "orderCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "order_code")
    private Long orderCode;
    
    @Column(nullable = false, length = 50)
    private String eventType; // "WEBHOOK", "CREATE_RESPONSE", "GET_RESPONSE", "CANCEL_RESPONSE"
    
    @Column(columnDefinition = "TEXT")
    private String payload; // Raw JSON response
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
