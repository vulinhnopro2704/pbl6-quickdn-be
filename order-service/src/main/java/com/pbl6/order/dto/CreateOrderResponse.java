package com.pbl6.order.dto;

import java.util.UUID;

public record CreateOrderResponse(UUID orderId, Double totalAmount, String currency, String status) {}