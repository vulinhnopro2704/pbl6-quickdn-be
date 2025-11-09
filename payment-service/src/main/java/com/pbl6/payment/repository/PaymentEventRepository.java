package com.pbl6.payment.repository;

import com.pbl6.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    
    List<PaymentEvent> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
    
    List<PaymentEvent> findByOrderCodeOrderByCreatedAtDesc(Long orderCode);
}
