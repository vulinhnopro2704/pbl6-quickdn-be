package com.pbl6.order.dto;


import com.pbl6.order.entity.PackageCategory;
import com.pbl6.order.entity.PackageSize;
import com.pbl6.order.entity.PayerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record PackageDto(
        @NotNull PackageSize size,
        @NotNull Double weightKg,
        @NotNull @Valid AddressDto receiverAddress,
        @NotNull PayerType payerType,
        @NotNull PackageCategory category,
        Boolean cod,
        Double codAmount,
        String description
) {}