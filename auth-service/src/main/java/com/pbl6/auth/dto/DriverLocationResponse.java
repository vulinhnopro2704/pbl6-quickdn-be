package com.pbl6.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverLocationResponse(
    UUID driverId,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalDateTime updatedAt) {}
