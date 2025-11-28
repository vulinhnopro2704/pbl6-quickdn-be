package com.pbl6.auth.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserUpdateRequest(
    @Size(max = 100) String fullName, LocalDate dob, String avatarUrl) {}
