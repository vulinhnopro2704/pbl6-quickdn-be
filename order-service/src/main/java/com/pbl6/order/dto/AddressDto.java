package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Address details used for pickup or receiver")
public record AddressDto(
        @Schema(description = "Detailed address line", example = "123 Le Loi, District 1", required = true)
        @NotBlank @Size(max = 500) String detail,

        @Schema(description = "Contact name at the address", example = "Store A", required = true)
        @NotBlank String name,

        @Schema(description = "Contact phone number", example = "0912345678", required = true)
        @NotBlank String phone,

        @Schema(description = "Latitude coordinate") Double latitude,

        @Schema(description = "Longitude coordinate") Double longitude
) {}