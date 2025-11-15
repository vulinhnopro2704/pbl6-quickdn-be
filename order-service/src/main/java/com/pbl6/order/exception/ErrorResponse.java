package com.pbl6.order.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard error response structure")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
                @Schema(description = "Error type/category", example = "Bad Request", required = true)
                String error,

                @Schema(description = "Detailed error message", example = "Phone number already exists", required = true)
                String message,

                @Schema(description = "HTTP status code", example = "400", required = true)
                int status,

                @Schema(description = "Timestamp when the error occurred", example = "2025-11-13T10:30:45")
                LocalDateTime timestamp,

                @Schema(description = "Request path where the error occurred", example = "/api/order/create")
                String path,

                @Schema(description = "Validation errors for field-specific issues", example = "{\"phone\": \"Invalid phone number format\"}")
                Map<String, String> validationErrors
) {
        // Constructor with required fields only
        public ErrorResponse(String error, String message, int status) {
                this(error, message, status, LocalDateTime.now(), null, null);
        }

        // Constructor without validation errors
        public ErrorResponse(String error, String message, int status, String path) {
                this(error, message, status, LocalDateTime.now(), path, null);
        }

        // Constructor with validation errors
        public ErrorResponse(String error, String message, int status, String path, Map<String, String> validationErrors) {
                this(error, message, status, LocalDateTime.now(), path, validationErrors);
        }
}
