package com.pbl6.auth.dto;

import com.pbl6.auth.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record DriverRegisterRequest(

        @NotBlank(message = "Vehicle plate number is required")
        @Size(max = 50)
        String vehiclePlateNumber,  // biển số xe

        @NotBlank(message = "License number is required")
        @Size(max = 100)
        String licenseNumber,

        @NotBlank(message = "Full name is required")
        @Size(max = 255)
        String identityFullName,

        @NotBlank(message = "Identity number is required")
        @Size(min = 9, max = 12)
        String identityNumber,

        @NotNull
        LocalDate identityIssueDate,

        @NotBlank
        @Size(max = 255)
        String identityIssuePlace,

        @NotBlank
        @Size(max = 500)
        String identityAddress,

        @NotNull
        Gender identityGender,

        @NotNull
        LocalDate identityBirthdate
) {}

