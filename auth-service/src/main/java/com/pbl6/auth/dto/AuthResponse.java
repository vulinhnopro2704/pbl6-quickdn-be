package com.pbl6.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {}
