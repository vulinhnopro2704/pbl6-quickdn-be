package com.pbl6.order.dto;

import com.pbl6.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderStatusUpdateRequest(
    @NotNull OrderStatus newStatus, String reasonNote, UUID newShipperId) {}
