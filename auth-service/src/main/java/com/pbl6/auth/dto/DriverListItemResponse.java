package com.pbl6.auth.dto;

import com.pbl6.auth.entity.DriverStatus;
import com.pbl6.auth.entity.Gender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverListItemResponse(
    UUID id,
    UUID userId,
    String vehiclePlateNumber,
    String licenseNumber,
    String identityFullName,
    Gender identityGender,
    DriverStatus status,
    BigDecimal ratingAvg,
    Integer ratingCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
