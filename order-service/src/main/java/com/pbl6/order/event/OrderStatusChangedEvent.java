package com.pbl6.order.event;

import com.pbl6.order.entity.OrderStatus;

import java.util.UUID;

public class OrderStatusChangedEvent {
  private final UUID orderId;
  private final UUID creatorId;
  private final UUID oldShipper;
  private final UUID newShipper;
  private final OrderStatus from;
  private final OrderStatus to;

  public OrderStatusChangedEvent(
      UUID orderId,
      UUID creatorId,
      UUID oldShipper,
      UUID newShipper,
      OrderStatus from,
      OrderStatus to) {
    this.orderId = orderId;
    this.creatorId = creatorId;
    this.oldShipper = oldShipper;
    this.newShipper = newShipper;
    this.from = from;
    this.to = to;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public UUID getOldShipper() {
    return oldShipper;
  }

  public UUID getNewShipper() {
    return newShipper;
  }

  public OrderStatus getFrom() {
    return from;
  }

  public OrderStatus getTo() {
    return to;
  }
}
