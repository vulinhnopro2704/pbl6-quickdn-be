package com.pbl6.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserAddressResponse(
    UUID id,
    String addressLine,
    String description,
    String detail,
    String name,
    String phone,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer wardCode,
    Integer districtCode,
    String note,
    boolean isPrimary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
