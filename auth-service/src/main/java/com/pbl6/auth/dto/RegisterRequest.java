package com.pbl6.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Request payload for user registration",
    example = "{\"phone\": \"0912345678\", \"password\": \"SecurePass123!\", \"fullName\": \"Nguyen Van A\"}"
)
public record RegisterRequest(
    @Schema(
        description = "User's phone number (Vietnamese format) - must be unique",
        example = "0912345678",
        required = true,
        minLength = 10,
        maxLength = 11,
        pattern = "^0[0-9]{9,10}$"
    )
    String phone,
    
    @Schema(
        description = "User's password - minimum 8 characters",
        example = "SecurePass123!",
        required = true,
        minLength = 8,
        format = "password"
    )
    String password,
    
    @Schema(
        description = "User's full name",
        example = "Nguyen Van A",
        required = true,
        minLength = 2,
        maxLength = 100
    )
    String fullName
) {}
