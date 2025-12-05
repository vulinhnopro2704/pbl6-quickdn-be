package com.pbl6.order.event;

import java.util.UUID;

public class OrderCreatedEvent {
  private final UUID orderId;
  private final double pickupLon;
  private final double pickupLat;

  //    private final boolean scheduled;

  public OrderCreatedEvent(UUID orderId, double pickupLon, double pickupLat
      //            , boolean scheduled
      ) {
    this.orderId = orderId;
    this.pickupLon = pickupLon;
    this.pickupLat = pickupLat;
    //        this.scheduled = scheduled;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public double getPickupLon() {
    return pickupLon;
  }

  public double getPickupLat() {
    return pickupLat;
  }
  //    public boolean isScheduled() { return scheduled; }
}
