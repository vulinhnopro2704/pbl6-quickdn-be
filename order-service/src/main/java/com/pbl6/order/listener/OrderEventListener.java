package com.pbl6.order.listener;

import com.pbl6.order.event.OrderCreatedEvent;
import com.pbl6.order.service.ShipperPushService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class OrderEventListener {

  private final ShipperPushService shipperPushService;

  public OrderEventListener(ShipperPushService shipperPushService) {
    this.shipperPushService = shipperPushService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCreated(OrderCreatedEvent evt) {
    int batchK = 3; // top-k per batch
    shipperPushService.pushToNearestShippersAsync(
        evt.getOrderId(), evt.getPickupLon(), evt.getPickupLat(), batchK);
  }
}
