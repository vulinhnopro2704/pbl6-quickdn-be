package com.pbl6.payment.client;

import com.pbl6.payment.dto.payos.PayosCreatePaymentRequest;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import com.pbl6.payment.dto.payos.PayosGetPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of PayosClient for testing
 * Stores payments in-memory without calling actual payOS API
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payos.api.mock-enabled", havingValue = "true")
public class PayosClientMock implements PayosClient {
    
    private final Map<String, PayosCreatePaymentResponse.PaymentData> payments = new ConcurrentHashMap<>();
    private long mockPaymentLinkIdCounter = 1000;
    
    @Override
    public PayosCreatePaymentResponse createPayment(PayosCreatePaymentRequest request) {
        log.info("[MOCK] Creating payment: orderCode={}, amount={}", 
            request.getOrderCode(), request.getAmount());
        
        String paymentLinkId = "mock_" + (mockPaymentLinkIdCounter++);
        String checkoutUrl = "https://pay.payos.vn/web/" + paymentLinkId;
        
        PayosCreatePaymentResponse.PaymentData data = PayosCreatePaymentResponse.PaymentData.builder()
            .paymentLinkId(paymentLinkId)
            .orderCode(request.getOrderCode())
            .amount(request.getAmount())
            .description(request.getDescription())
            .checkoutUrl(checkoutUrl)
            .status("PENDING")
            .expiredAt(request.getExpiredAt())
            .currency("VND")
            .qrCode("mock_qr_code")
            .build();
        
        payments.put(paymentLinkId, data);
        payments.put(request.getOrderCode().toString(), data);
        
        return PayosCreatePaymentResponse.builder()
            .code("00")
            .desc("success")
            .data(data)
            .signature("mock_signature")
            .build();
    }
    
    @Override
    public PayosGetPaymentResponse getPayment(String id) {
        log.info("[MOCK] Getting payment: id={}", id);
        
        PayosCreatePaymentResponse.PaymentData paymentData = payments.get(id);
        
        if (paymentData == null) {
            throw new RuntimeException("Không tìm thấy thanh toán: " + id);
        }
        
        PayosGetPaymentResponse.PaymentInfo info = PayosGetPaymentResponse.PaymentInfo.builder()
            .id(paymentData.getPaymentLinkId())
            .orderCode(paymentData.getOrderCode())
            .amount(paymentData.getAmount())
            .amountPaid(0L)
            .amountRemaining(paymentData.getAmount())
            .status(paymentData.getStatus())
            .build();
        
        return PayosGetPaymentResponse.builder()
            .code("00")
            .desc("success")
            .data(info)
            .signature("mock_signature")
            .build();
    }
    
    @Override
    public PayosGetPaymentResponse cancelPayment(String id, String cancellationReason) {
        log.info("[MOCK] Cancelling payment: id={}, reason={}", id, cancellationReason);
        
        PayosCreatePaymentResponse.PaymentData paymentData = payments.get(id);
        
        if (paymentData == null) {
            throw new RuntimeException("Không tìm thấy thanh toán: " + id);
        }
        
        // Update status to CANCELLED
        paymentData.setStatus("CANCELLED");
        
        PayosGetPaymentResponse.PaymentInfo info = PayosGetPaymentResponse.PaymentInfo.builder()
            .id(paymentData.getPaymentLinkId())
            .orderCode(paymentData.getOrderCode())
            .amount(paymentData.getAmount())
            .status("CANCELLED")
            .cancellationReason(cancellationReason)
            .build();
        
        return PayosGetPaymentResponse.builder()
            .code("00")
            .desc("success")
            .data(info)
            .signature("mock_signature")
            .build();
    }
    
    /**
     * Helper method for testing: simulate payment completion
     */
    public void simulatePaymentSuccess(String paymentLinkId) {
        PayosCreatePaymentResponse.PaymentData data = payments.get(paymentLinkId);
        if (data != null) {
            data.setStatus("PAID");
        }
    }
}
