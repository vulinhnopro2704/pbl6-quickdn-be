package com.pbl6.order.event;

import java.util.UUID;

public class OrderAssignedEvent {
    private final UUID orderId;
    private final UUID creatorId;
    public OrderAssignedEvent(UUID orderId, UUID creatorId) {
        this.orderId = orderId;
        this.creatorId = creatorId;
    }
    public UUID getOrderId() { return orderId; }
    public UUID getCreatorId() { return creatorId; }
}
