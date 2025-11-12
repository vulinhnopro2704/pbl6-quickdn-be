package com.pbl6.payment.exception;

/**
 * Exception thrown when webhook signature verification fails
 */
public class InvalidWebhookSignatureException extends RuntimeException {
    
    public InvalidWebhookSignatureException(String message) {
        super(message);
    }
}
