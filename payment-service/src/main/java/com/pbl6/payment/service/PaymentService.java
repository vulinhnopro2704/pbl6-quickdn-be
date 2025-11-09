package com.pbl6.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.payment.client.PayosClient;
import com.pbl6.payment.config.PayosConfig;
import com.pbl6.payment.dto.CreatePaymentRequest;
import com.pbl6.payment.dto.PaymentResponse;
import com.pbl6.payment.dto.payos.PayosCreatePaymentRequest;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import com.pbl6.payment.entity.Payment;
import com.pbl6.payment.entity.PaymentEvent;
import com.pbl6.payment.entity.PaymentStatus;
import com.pbl6.payment.exception.PayosApiException;
import com.pbl6.payment.repository.PaymentEventRepository;
import com.pbl6.payment.repository.PaymentRepository;
import com.pbl6.payment.util.SignatureHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

/**
 * Payment service - core business logic
 * Handles payment creation with idempotency by orderCode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PayosClient payosClient;
    private final PayosConfig payosConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * Create payment with idempotency by orderCode
     * 
     * Flow:
     * 1. Check if payment with orderCode already exists and is not terminal status
     * 2. If exists and PENDING/PAID, return existing payment (idempotency)
     * 3. Otherwise, create local payment record with PENDING status
     * 4. Build payOS request with signature
     * 5. Call payOS API to create payment link
     * 6. Update local payment with paymentLinkId and checkoutUrl
     * 7. Save payOS response as PaymentEvent
     * 8. Return payment response to client
     * 
     * @param request Payment creation request from client
     * @return Payment response with checkout URL
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment: orderCode={}, amount={}", 
            request.getOrderCode(), request.getAmount());
        
        // 1. Check for existing payment (idempotency by orderCode)
        Optional<Payment> existingPayment = paymentRepository.findByOrderCode(request.getOrderCode());
        
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            
            // If payment is in non-terminal status (PENDING or PAID), return it
            if (payment.getStatus() == PaymentStatus.PENDING || 
                payment.getStatus() == PaymentStatus.PAID) {
                
                log.info("Returning existing payment: orderCode={}, status={}", 
                    request.getOrderCode(), payment.getStatus());
                
                return buildPaymentResponse(payment);
            }
            
            // If CANCELLED or FAILED, allow creating new payment with same orderCode
            log.info("Previous payment was {}, creating new payment", payment.getStatus());
        }
        
        // 2. Create local payment record with PENDING status
        Payment payment = Payment.builder()
            .orderCode(request.getOrderCode())
            .amount(request.getAmount())
            .description(request.getDescription())
            .status(PaymentStatus.PENDING)
            .build();
        
        payment = paymentRepository.save(payment);
        log.info("Created local payment record: id={}, orderCode={}", 
            payment.getId(), payment.getOrderCode());
        
        try {
            // 3. Build payOS request with signature
            PayosCreatePaymentRequest payosRequest = buildPayosRequest(request);
            
            // 4. Call payOS API
            PayosCreatePaymentResponse payosResponse = payosClient.createPayment(payosRequest);
            
            // 5. Save payOS response as event
            savePaymentEvent(payment.getId(), request.getOrderCode(), 
                "CREATE_RESPONSE", payosResponse);
            
            // 6. Update payment with payOS response data
            payment.setPayosPaymentLinkId(payosResponse.getData().getPaymentLinkId());
            payment.setCheckoutUrl(payosResponse.getData().getCheckoutUrl());
            payment.setExpiredAt(payosResponse.getData().getExpiredAt());
            payment = paymentRepository.save(payment);
            
            log.info("Payment created successfully: paymentId={}, paymentLinkId={}, checkoutUrl={}", 
                payment.getId(), payment.getPayosPaymentLinkId(), payment.getCheckoutUrl());
            
            return buildPaymentResponse(payment);
            
        } catch (PayosApiException e) {
            // Mark payment as FAILED
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            
            log.error("Failed to create payment in payOS: orderCode={}", 
                request.getOrderCode(), e);
            
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get payment by orderCode
     */
    public Optional<Payment> getPaymentByOrderCode(Long orderCode) {
        return paymentRepository.findByOrderCode(orderCode);
    }
    
    /**
     * Get payment by paymentLinkId
     */
    public Optional<Payment> getPaymentByPaymentLinkId(String paymentLinkId) {
        return paymentRepository.findByPayosPaymentLinkId(paymentLinkId);
    }
    
    /**
     * Build payOS API request from our request
     * Includes signature generation
     */
    private PayosCreatePaymentRequest buildPayosRequest(CreatePaymentRequest request) {
        // Use provided URLs or fall back to defaults
        String cancelUrl = request.getCancelUrl() != null ? 
            request.getCancelUrl() : payosConfig.getDefaultCancelUrl();
        String returnUrl = request.getReturnUrl() != null ? 
            request.getReturnUrl() : payosConfig.getDefaultReturnUrl();
        
        // Generate signature as per payOS docs
        // Data format (alphabetically sorted):
        // amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
        String signature = SignatureHelper.generatePaymentSignature(
            request.getAmount(),
            cancelUrl,
            request.getDescription(),
            request.getOrderCode(),
            returnUrl,
            payosConfig.getChecksumKey()
        );
        
        log.debug("Generated signature for orderCode={}: {}", request.getOrderCode(), signature);
        
        // Build payOS request
        PayosCreatePaymentRequest.PayosCreatePaymentRequestBuilder builder = PayosCreatePaymentRequest.builder()
            .orderCode(request.getOrderCode())
            .amount(request.getAmount())
            .description(request.getDescription())
            .cancelUrl(cancelUrl)
            .returnUrl(returnUrl)
            .signature(signature);
        
        // Add optional fields
        if (request.getExpiredAt() != null) {
            builder.expiredAt(request.getExpiredAt());
        }
        
        if (request.getBuyer() != null) {
            builder.buyerName(request.getBuyer().getName())
                .buyerCompanyName(request.getBuyer().getCompanyName())
                .buyerTaxCode(request.getBuyer().getTaxCode())
                .buyerAddress(request.getBuyer().getAddress())
                .buyerEmail(request.getBuyer().getEmail())
                .buyerPhone(request.getBuyer().getPhone());
        }
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            builder.items(request.getItems());
        }
        
        if (request.getInvoice() != null) {
            builder.invoice(request.getInvoice());
        }
        
        return builder.build();
    }
    
    /**
     * Build payment response for client
     */
    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
            .paymentId(payment.getId())
            .orderCode(payment.getOrderCode())
            .amount(payment.getAmount())
            .description(payment.getDescription())
            .checkoutUrl(payment.getCheckoutUrl())
            .paymentLinkId(payment.getPayosPaymentLinkId())
            .status(payment.getStatus())
            .expiredAt(payment.getExpiredAt())
            .build();
    }
    
    /**
     * Save payment event for audit trail
     */
    private void savePaymentEvent(Long paymentId, Long orderCode, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .orderCode(orderCode)
                .eventType(eventType)
                .payload(payloadJson)
                .build();
            
            paymentEventRepository.save(event);
            
        } catch (Exception e) {
            log.error("Failed to save payment event: paymentId={}, eventType={}", 
                paymentId, eventType, e);
            // Don't fail the main flow if event logging fails
        }
    }
}
