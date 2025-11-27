package com.pbl6.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserAddressResponse(
    UUID id,
    String addressLine,
    String ward,
    String district,
    String province,
    String country,
    BigDecimal latitude,
    BigDecimal longitude,
    String note,
    boolean isPrimary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
