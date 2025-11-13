package com.pbl6.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for resource not found errors (404)
 */
public class ResourceNotFoundException extends AuthException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, String error) {
        super(message, error, HttpStatus.NOT_FOUND);
    }
}
