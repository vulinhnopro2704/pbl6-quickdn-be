package com.pbl6.order.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AddressDto(
        @NotBlank @Size(max = 500) String detail,
        @NotBlank String name,
        @NotBlank String phone,
        Double latitude,
        Double longitude
) {}