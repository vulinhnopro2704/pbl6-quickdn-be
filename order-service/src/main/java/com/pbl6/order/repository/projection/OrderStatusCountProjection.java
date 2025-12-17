package com.pbl6.order.repository.projection;

import com.pbl6.order.entity.OrderStatus;

public interface OrderStatusCountProjection {
  OrderStatus getStatus();
  long getCount();
}
