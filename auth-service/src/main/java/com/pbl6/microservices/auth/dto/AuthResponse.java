package com.pbl6.microservices.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {}
