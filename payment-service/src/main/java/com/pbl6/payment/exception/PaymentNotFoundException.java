package com.pbl6.payment.exception;

/**
 * Exception thrown when payment is not found by orderCode or paymentId
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(Long orderCode) {
        super("Payment not found with orderCode: " + orderCode);
    }
}
