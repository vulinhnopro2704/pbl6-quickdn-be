package com.pbl6.payment.repository;

import com.pbl6.payment.entity.Payment;
import com.pbl6.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by orderCode for idempotency check
     */
    Optional<Payment> findByOrderCode(Long orderCode);
    
    /**
     * Find payment by payOS payment link ID
     */
    Optional<Payment> findByPayosPaymentLinkId(String payosPaymentLinkId);
    
    /**
     * Check if payment exists with given orderCode and non-terminal status
     */
    boolean existsByOrderCodeAndStatusNotIn(Long orderCode, java.util.List<PaymentStatus> statuses);
}
