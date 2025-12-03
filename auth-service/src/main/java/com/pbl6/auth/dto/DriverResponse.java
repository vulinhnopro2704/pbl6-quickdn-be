package com.pbl6.auth.dto;

import com.pbl6.auth.entity.DriverStatus;
import com.pbl6.auth.entity.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID userId,
        String vehiclePlateNumber,
        String avatarUrl,
        String licenseNumber,

        String identityFullName,
        String identityNumber,
        LocalDate identityIssueDate,
        String identityIssuePlace,
        String identityAddress,
        Gender identityGender,
        LocalDate identityBirthdate,

        DriverStatus status,
        BigDecimal ratingAvg,
        Integer ratingCount,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}