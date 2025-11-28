package com.pbl6.order.dto;

import com.pbl6.order.entity.PackageCategory;
import com.pbl6.order.entity.PackageSize;
import com.pbl6.order.entity.PayerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Package item within an order")
public record PackageDto(
    @Schema(description = "Size enum for the package") @NotNull PackageSize size,
    @Schema(description = "Weight in kilograms") @NotNull Double weightKg,
    @Schema(description = "Receiver address for this package") @NotNull @Valid
        AddressDto receiverAddress,
    @Schema(description = "Who pays for shipping") @NotNull PayerType payerType,
    @Schema(description = "Category of the package") PackageCategory category,
    @Schema(description = "Is COD enabled for this package") Boolean cod,
    @Schema(description = "COD amount") Double codAmount,
    @Schema(description = "Image URL of the package") @NotBlank String imageUrl,
    @Schema(description = "Optional description") String description) {}
