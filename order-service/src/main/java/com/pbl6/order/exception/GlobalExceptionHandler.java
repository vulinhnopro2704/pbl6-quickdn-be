package com.pbl6.order.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // App-level exceptions (business)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                ex.getStatus().value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // Validation errors from @Valid on @RequestBody (DTO fields)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
            (a, b) -> a + "; " + b
        ));

    ErrorResponse body = new ErrorResponse(
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        HttpStatus.BAD_REQUEST.value(),
        request.getRequestURI(),
        details
    );
    return ResponseEntity.badRequest().body(body);
    }

    // Constraint violations on single params, path variables etc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath().toString();
            details.put(path, v.getMessage());
        }
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Constraint violation",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    // JSON parse issues (invalid JSON or field type mismatch)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String msg = "Malformed JSON request";
        Throwable cause = ex.getCause();
        Map<String, String> details = null;

        if (cause instanceof InvalidFormatException ife) {
            // invalid enum/value -> give hint about field and allowed values
            String field = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull).collect(Collectors.joining("."));
            String value = String.valueOf(ife.getValue());
            msg = String.format("Invalid value '%s' for field '%s'", value, field);
            details = Map.of(field, "invalid value or type");
        } else {
            // default include exception text minimally
            msg = ex.getMessage();
        }

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                msg,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Missing request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Missing parameter: " + ex.getParameterName(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                Map.of(ex.getParameterName(), "required")
        );
        return ResponseEntity.badRequest().body(body);
    }

    // fallback - unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
