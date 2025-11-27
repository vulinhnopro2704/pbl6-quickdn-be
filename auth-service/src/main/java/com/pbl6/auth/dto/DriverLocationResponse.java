package com.pbl6.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DriverLocationResponse(
    UUID driverId,
    double latitude,
    double longitude,
    LocalDateTime recordedAt,
    LocalDateTime updatedAt) {}
