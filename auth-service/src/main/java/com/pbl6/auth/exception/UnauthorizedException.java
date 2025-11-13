package com.pbl6.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for unauthorized access errors (401)
 */
public class UnauthorizedException extends AuthException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, String error) {
        super(message, error, HttpStatus.UNAUTHORIZED);
    }
}
