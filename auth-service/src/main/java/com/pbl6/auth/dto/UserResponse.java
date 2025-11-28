package com.pbl6.auth.dto;

import com.pbl6.auth.entity.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String phone,
    String fullName,
    LocalDate dob,
    Set<Role> roles,
    boolean enabled,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String avatarUrl,
    Set<UserAddressResponse> addresses) {}
