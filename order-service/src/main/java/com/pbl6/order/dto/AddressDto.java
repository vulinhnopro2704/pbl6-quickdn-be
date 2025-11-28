package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Address details used for pickup or receiver")
public record AddressDto(
    @Schema(
            description = "Detailed address line",
            example = "123 Le Loi, District 1",
            required = true)
        @NotBlank
        @Size(max = 500)
        String detail,
    @Schema(description = "Contact name at the address", example = "Store A", required = true)
        @NotBlank
        String name,
    @Schema(
            description = "User's phone number (Vietnamese format)",
            example = "0912345678",
            required = true,
            minLength = 10,
            maxLength = 11,
            pattern = "^0[0-9]{9,10}$")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^0\\d{9,10}$", message = "Invalid phone number format")
        @NotBlank
        String phone,
    @Schema(description = "Additional notes for the address", example = "Near school")
        @Size(max = 500)
        String note,
    @NotNull @Schema(description = "Latitude coordinate") Double latitude,
    @NotNull @Schema(description = "Longitude coordinate") Double longitude) {}
