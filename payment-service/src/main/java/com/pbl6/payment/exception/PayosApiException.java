package com.pbl6.payment.exception;

/**
 * Exception thrown when payOS API call fails
 */
public class PayosApiException extends RuntimeException {
    
    public PayosApiException(String message) {
        super(message);
    }
    
    public PayosApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
