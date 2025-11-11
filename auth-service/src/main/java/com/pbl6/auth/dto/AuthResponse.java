package com.pbl6.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Authentication response containing access token, refresh token, and token type",
    example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"tokenType\": \"Bearer\"}"
)
public record AuthResponse(
    @Schema(
        description = "JWT access token for API authentication - expires based on JWT_ACCESS_EXP_MS configuration",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        required = true
    )
    String accessToken,
    
    @Schema(
        description = "JWT refresh token for obtaining new access tokens - expires based on JWT_REFRESH_EXP_MS configuration",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYzMjU0MjJ9.4Adcj0vgH0y8yC3_Cx0eKT8fwpMeJf36POk6yJV_Qss",
        required = true
    )
    String refreshToken,
    
    @Schema(
        description = "Type of the token - always 'Bearer' for JWT tokens",
        example = "Bearer",
        required = true,
        defaultValue = "Bearer"
    )
    String tokenType
) {}
