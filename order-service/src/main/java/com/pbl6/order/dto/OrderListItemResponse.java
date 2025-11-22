package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderListItemResponse(
    UUID id,
    BigDecimal totalAmount,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime scheduledAt) {}
