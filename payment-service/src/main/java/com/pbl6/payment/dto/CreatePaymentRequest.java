package com.pbl6.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for our backend API: POST /api/payments
 * This is what clients send to our service (not directly to payOS)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CreatePaymentRequest",
    description = "Request to create a payment link via payOS gateway"
)
public class CreatePaymentRequest {
    
    @Schema(
        description = "Unique order code for idempotency. Must be unique across all payments.",
        example = "123456789",
        required = true,
        minimum = "1"
    )
    @NotNull(message = "orderCode is required")
    @Min(value = 1, message = "orderCode must be positive")
    private Long orderCode;
    
    @Schema(
        description = "Payment amount in VND (Vietnamese Dong). Must be positive integer.",
        example = "50000",
        required = true,
        minimum = "1000"
    )
    @NotNull(message = "amount is required")
    @Min(value = 1000, message = "amount must be at least 1000 VND")
    private Long amount;
    
    @Schema(
        description = "Description of the payment. Will be shown to customer during checkout.",
        example = "Payment for Order #12345 - 2 items",
        required = true,
        maxLength = 1000
    )
    @NotBlank(message = "description is required and cannot be blank")
    private String description;
    
    @Schema(
        description = "URL to redirect when customer cancels payment",
        example = "https://myapp.com/payment/cancel?orderId=12345",
        required = true
    )
    @NotBlank(message = "cancelUrl is required")
    private String cancelUrl;
    
    @Schema(
        description = "URL to redirect when payment succeeds",
        example = "https://myapp.com/payment/success?orderId=12345",
        required = true
    )
    @NotBlank(message = "returnUrl is required")
    private String returnUrl;
    
    @Schema(
        description = "Payment link expiration time as Unix timestamp (seconds). If not provided, uses default from config.",
        example = "1731024000",
        required = false
    )
    private Long expiredAt;
    
    @Schema(
        description = "Buyer information for invoice generation (optional)",
        required = false
    )
    @Valid
    private BuyerDto buyer;
    
    @Schema(
        description = "List of items in the order (optional). Used for invoice generation.",
        required = false
    )
    @Valid
    private List<ItemDto> items;
    
    @Schema(
        description = "Invoice configuration (optional)",
        required = false
    )
    @Valid
    private InvoiceDto invoice;
}
