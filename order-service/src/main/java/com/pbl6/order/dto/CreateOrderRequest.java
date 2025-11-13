package com.pbl6.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull @Valid AddressDto pickupAddress,
        @NotNull @Valid List<@Valid PackageDto> packages,
        String customerNote,
        String voucherCode,
        Boolean fragile,
        Boolean thermalBag,
        Boolean returnToPickupWhenCod,
        String scheduledAt
) {}
