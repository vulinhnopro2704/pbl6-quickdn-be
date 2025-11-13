package com.pbl6.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for authentication-related errors
 */
public class AuthException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = status.getReasonPhrase();
    }

    public AuthException(String message, String error, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
