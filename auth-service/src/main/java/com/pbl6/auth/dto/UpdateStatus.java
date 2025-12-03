package com.pbl6.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatus(@NotNull Boolean isAvailable, String fcmToken) {}
