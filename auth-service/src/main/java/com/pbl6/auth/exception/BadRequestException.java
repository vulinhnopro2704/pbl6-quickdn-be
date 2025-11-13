package com.pbl6.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for bad request errors (400)
 */
public class BadRequestException extends AuthException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, String error) {
        super(message, error, HttpStatus.BAD_REQUEST);
    }
}
