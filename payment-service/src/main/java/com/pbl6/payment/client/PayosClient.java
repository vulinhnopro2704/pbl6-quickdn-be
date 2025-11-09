package com.pbl6.payment.client;

import com.pbl6.payment.dto.payos.PayosCreatePaymentRequest;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import com.pbl6.payment.dto.payos.PayosGetPaymentResponse;

/**
 * PayOS API client interface
 * Provides methods to interact with payOS payment APIs
 */
public interface PayosClient {
    
    /**
     * Create a payment link in payOS
     * POST /v2/payment-requests
     * 
     * @param request Payment creation request
     * @return Payment creation response with checkoutUrl and paymentLinkId
     */
    PayosCreatePaymentResponse createPayment(PayosCreatePaymentRequest request);
    
    /**
     * Get payment information by orderCode or paymentLinkId
     * GET /v2/payment-requests/{id}
     * 
     * @param id Order code or payment link ID
     * @return Payment information
     */
    PayosGetPaymentResponse getPayment(String id);
    
    /**
     * Cancel a payment link
     * POST /v2/payment-requests/{id}/cancel
     * 
     * @param id Order code or payment link ID
     * @param cancellationReason Reason for cancellation
     * @return Cancelled payment information
     */
    PayosGetPaymentResponse cancelPayment(String id, String cancellationReason);
}
