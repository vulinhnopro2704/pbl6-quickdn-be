package com.pbl6.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Request payload for user login",
    example = "{\"phone\": \"0912345678\", \"password\": \"SecurePass123!\"}"
)
public record LoginRequest(
    @Schema(
        description = "User's phone number (Vietnamese format)",
        example = "0912345678",
        required = true,
        minLength = 10,
        maxLength = 11,
        pattern = "^0[0-9]{9,10}$"
    )
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Invalid phone number format")
    String phone,
    
    @Schema(
        description = "User's password",
        example = "SecurePass123!",
        required = true,
        minLength = 8,
        format = "password"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
