package com.pbl6.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
    String phone,
    
    @Schema(
        description = "User's password",
        example = "SecurePass123!",
        required = true,
        minLength = 8,
        format = "password"
    )
    String password
) {}
