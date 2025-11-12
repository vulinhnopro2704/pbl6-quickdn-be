package com.pbl6.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO
 * Matches the format shown in OpenAPI docs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {
    
    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-11-07T10:30:00Z"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    
    @Schema(
        description = "HTTP status code",
        example = "400"
    )
    private int status;
    
    @Schema(
        description = "Error type/title",
        example = "Bad Request"
    )
    private String error;
    
    @Schema(
        description = "Detailed error message",
        example = "orderCode is required"
    )
    private String message;
    
    @Schema(
        description = "Request path that caused the error",
        example = "/api/payments"
    )
    private String path;
}
