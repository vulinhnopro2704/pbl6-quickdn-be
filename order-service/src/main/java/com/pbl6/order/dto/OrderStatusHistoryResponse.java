package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusHistoryResponse(
    UUID id,
    OrderStatus fromStatus,
    OrderStatus toStatus,
    UUID changedBy,
    String reason,
    UUID oldShipperId,
    UUID newShipperId,
    LocalDateTime createdAt) {}
