package com.pbl6.order.dto;

import com.pbl6.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request to create an order")
public record CreateOrderRequest(
    @Schema(description = "Pickup address for the order", required = true) @NotNull @Valid
        AddressDto pickupAddress,
    @Schema(description = "List of packages within the order", required = true)
        @NotNull
        @Valid
        @NotEmpty
        List<@Valid PackageDto> packages,
    @Schema(description = "Customer note for the order") String customerNote,
    @Schema(description = "Voucher code applied to the order") String voucherCode,
    @Schema(description = "Whether the package is fragile") Boolean fragile,
    @Schema(description = "Whether a thermal bag is requested") Boolean thermalBag,
    @Schema(description = "Return to pickup location when COD fails") Boolean returnToPickupWhenCod,
    @Schema(description = "Scheduled pickup time in ISO-8601 format") String scheduledAt,
    @Schema(description = "Payment method for the order") PaymentMethod paymentMethod) {}
