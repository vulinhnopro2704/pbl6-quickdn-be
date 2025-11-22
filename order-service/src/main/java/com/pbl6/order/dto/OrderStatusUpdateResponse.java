package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusUpdateResponse(
    UUID orderId,
    OrderStatus oldStatus,
    OrderStatus newStatus,
    LocalDateTime changedAt,
    String reasonCode,
    String reasonNote) {}
