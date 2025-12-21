package com.pbl6.order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID orderId,
        UUID reviewerId,
        UUID shipperId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
