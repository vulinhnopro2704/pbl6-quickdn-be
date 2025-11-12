package com.pbl6.payment.exception;

/**
 * Exception thrown when payment creation fails
 */
public class PaymentCreationException extends RuntimeException {
    
    public PaymentCreationException(String message) {
        super(message);
    }
    
    public PaymentCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
