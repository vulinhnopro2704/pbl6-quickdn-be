package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after creating an order")
public record CreateOrderResponse(
    @Schema(description = "Order identifier") UUID orderId,
    @Schema(description = "Total amount for the order") Double totalAmount,
    @Schema(description = "Currency code") String currency,
    @Schema(description = "Order status") OrderStatus status) {}
