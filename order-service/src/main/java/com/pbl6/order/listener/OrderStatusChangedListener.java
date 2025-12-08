package com.pbl6.order.listener;

import com.pbl6.order.dto.NotifyPayload;
import com.pbl6.order.entity.OrderStatus;
import com.pbl6.order.event.OrderStatusChangedEvent;
import com.pbl6.order.service.FirebaseMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static com.pbl6.order.constant.RedisKeyConstants.DRIVER_FCM_TOKEN;
import static com.pbl6.order.constant.RedisKeyConstants.USER_FCM_TOKEN;
import static com.pbl6.order.entity.OrderStatus.*;

@Component
public class OrderStatusChangedListener {

  private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedListener.class);
  private final RedisTemplate<String, String> redisTemplate;
  private final FirebaseMessagingService firebaseMessagingService;
  private final ExecutorService pushExecutor;

  public OrderStatusChangedListener(
      RedisTemplate<String, String> redisTemplate,
      FirebaseMessagingService firebaseMessagingService,
      ExecutorService pushExecutor) {
    this.redisTemplate = redisTemplate;
    this.firebaseMessagingService = firebaseMessagingService;
    this.pushExecutor = pushExecutor;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderStatusChanged(OrderStatusChangedEvent ev) {
    UUID creatorId = ev.getCreatorId();
    UUID assignedShipper = ev.getNewShipper();
    UUID orderId = ev.getOrderId();
    OrderStatus to = ev.getTo();

    // --- build payloads (same logic như bạn) ---
    NotifyPayload userPayload = buildUserPayload(to, orderId, assignedShipper);
    NotifyPayload driverPayload = buildDriverPayload(to, orderId, assignedShipper);

    // submit to executor (non-blocking)
    try {
      pushExecutor.submit(
          () -> {
            // notify user
            if (userPayload != null && creatorId != null) {
              try {
                String userToken =
                    redisTemplate.opsForValue().get(String.format(USER_FCM_TOKEN, creatorId));
                if (userToken != null && !userToken.isEmpty()) {
                  firebaseMessagingService.sendNotificationWithData(
                      userToken, userPayload.title, userPayload.body, userPayload.data);
                  log.info("Sent user notification for order={} user={}", orderId, creatorId);
                } else {
                  log.debug("No user token for user={}, order={}", creatorId, orderId);
                }
              } catch (Exception ex) {
                log.error(
                    "Error sending user notification for order={} user={}", orderId, creatorId, ex);
              }
            }

            // notify driver
            if (driverPayload != null && assignedShipper != null) {
              try {
                  String driverKey = String.format(DRIVER_FCM_TOKEN, assignedShipper);
                String driverToken =
                    redisTemplate
                        .opsForValue()
                        .get(String.format(driverKey));
                if (driverToken != null && !driverToken.isEmpty()) {
                  firebaseMessagingService.sendNotificationWithData(
                      driverToken, driverPayload.title, driverPayload.body, driverPayload.data);
                  log.info(
                      "Sent driver notification for order={} driver={}", orderId, assignedShipper);
                } else {
                  log.debug("No driver token for driver={}, order={}", assignedShipper, orderId);
                }
              } catch (Exception ex) {
                log.error(
                    "Error sending driver notification for order={} driver={}",
                    orderId,
                    assignedShipper,
                    ex);
              }
            }
          });
    } catch (Exception submitEx) {
      // fallback synchronous best-effort
      log.warn("Push executor rejected, falling back sync for order={}", orderId, submitEx);
      // user fallback
      if (userPayload != null && creatorId != null) {
        try {
          String userToken =
              redisTemplate.opsForValue().get(String.format(USER_FCM_TOKEN, creatorId));
          if (userToken != null && !userToken.isEmpty()) {
            firebaseMessagingService.sendNotificationWithData(
                userToken, userPayload.title, userPayload.body, userPayload.data);
          }
        } catch (Exception ex) {
          log.error("Fallback user push failed for order={} user={}", orderId, creatorId, ex);
        }
      }
      // driver fallback
      if (driverPayload != null && assignedShipper != null) {
        try {
          String driverToken =
              redisTemplate.opsForValue().get(String.format(DRIVER_FCM_TOKEN, assignedShipper));
          if (driverToken != null && !driverToken.isEmpty()) {
            firebaseMessagingService.sendNotificationWithData(
                driverToken, driverPayload.title, driverPayload.body, driverPayload.data);
          }
        } catch (Exception ex) {
          log.error(
              "Fallback driver push failed for order={} driver={}", orderId, assignedShipper, ex);
        }
      }
    }
  }

  // ---------------- helper builders (dùng same logic bạn đã viết) ----------------
  private NotifyPayload buildUserPayload(
      OrderStatus to, UUID orderIdForNotify, UUID assignedShipper) {
    if (to == null) return null;
    String title, body;
    Map<String, String> data;
    switch (to) {
      case REASSIGNING_DRIVER -> {
        title = "🔄 Đang tìm tài xế mới";
        body = "Đơn hàng của bạn đang được tìm tài xế mới, vui lòng chờ.";
        data = buildDataMap("ORDER_REASSIGNING", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case DRIVER_ASSIGNED -> {
        if (assignedShipper != null) {
          title = "✅ Đã có tài xế nhận đơn";
          body = "Tài xế đang được liên hệ để tới lấy hàng.";
          data =
              buildDataMap(
                  "DRIVER_ASSIGNED", orderIdForNotify, title, body, assignedShipper.toString());
          return new NotifyPayload(title, body, data);
        }
        return null;
      }
      case DRIVER_EN_ROUTE_PICKUP -> {
        title = "🚗 Tài xế đang tới điểm lấy";
        body = "Tài xế đang trên đường tới điểm lấy hàng.";
        data = buildDataMap("DRIVER_EN_ROUTE_PICKUP", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case ARRIVED_PICKUP -> {
        title = "📍 Tài xế đã tới điểm lấy";
        body = "Tài xế đã tới địa điểm lấy hàng.";
        data = buildDataMap("ARRIVED_PICKUP", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case PACKAGE_PICKED -> {
        title = "📦 Đã lấy hàng";
        body = "Tài xế đã lấy hàng và chuẩn bị giao.";
        data = buildDataMap("PICKUP_SUCCESS", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case EN_ROUTE_DELIVERY -> {
        title = "🚚 Đang giao hàng";
        body = "Tài xế đang di chuyển đến địa chỉ giao hàng.";
        data = buildDataMap("EN_ROUTE_DELIVERY", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case ARRIVED_DELIVERY -> {
        title = "📍 Đã đến nơi giao hàng";
        body = "Tài xế đã tới địa điểm giao hàng.";
        data = buildDataMap("ARRIVED_DELIVERY", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case DELIVERED -> {
        title = "🎉 Giao hàng thành công";
        body = "Đơn hàng đã được giao thành công. Cảm ơn bạn!";
        data = buildDataMap("DELIVERED", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      case CANCELLED_BY_DRIVER, CANCELLED_BY_SENDER, CANCELLED_NO_DRIVER, ORDER_CANCELLED -> {
        title = "❌ Đơn hàng bị hủy";
        body = "Đơn hàng của bạn đã bị hủy. Vui lòng kiểm tra chi tiết.";
        data = buildDataMap("ORDER_CANCELLED", orderIdForNotify, title, body, null);
        return new NotifyPayload(title, body, data);
      }
      default -> {
        return null;
      }
    }
  }

  private NotifyPayload buildDriverPayload(
      OrderStatus to, UUID orderIdForNotify, UUID assignedShipper) {
    if (to == null || assignedShipper == null) return null;
    String title, body;
    Map<String, String> data;
    String assignedShipperId = assignedShipper.toString();
    switch (to) {
      case DRIVER_ASSIGNED -> {
        title = "📦 Bạn được giao một đơn hàng";
        body = "Bạn vừa được gán đơn, vui lòng vào app xem và xác nhận.";
        data = buildDataMap("ASSIGNED_ORDER", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case DRIVER_EN_ROUTE_PICKUP -> {
        title = "🚗 Đến điểm lấy";
        body = "Vui lòng di chuyển tới điểm lấy hàng.";
        data = buildDataMap("EN_ROUTE_PICKUP", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case ARRIVED_PICKUP -> {
        title = "📍 Đã đến điểm lấy";
        body = "Bạn đã đến điểm lấy. Vui lòng liên hệ người gửi nếu cần.";
        data = buildDataMap("ARRIVED_PICKUP", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case PACKAGE_PICKED -> {
        title = "📦 Đã lấy hàng";
        body = "Bạn đã xác nhận lấy hàng. Hãy chuyển sang giao hàng.";
        data = buildDataMap("PICKUP_CONFIRMED", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case EN_ROUTE_DELIVERY -> {
        title = "🚚 Đang giao";
        body = "Vui lòng giao hàng tới địa chỉ người nhận.";
        data = buildDataMap("EN_ROUTE_DELIVERY", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case DELIVERED -> {
        title = "✅ Giao hàng xong";
        body = "Bạn đã hoàn thành giao hàng. Cảm ơn!";
        data = buildDataMap("DELIVERED", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case REASSIGNING_DRIVER -> {
        title = "🔄 Đang reassign";
        body = "Đơn hàng này đang được tìm tài xế mới. Vui lòng chờ.";
        data = buildDataMap("REASSIGNING", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      case CANCELLED_BY_DRIVER, CANCELLED_BY_SENDER, ORDER_CANCELLED -> {
        title = "❌ Đơn hàng bị hủy";
        body = "Đơn hàng đã bị hủy. Không cần thực hiện nhiệm vụ này nữa.";
        data = buildDataMap("ORDER_CANCELLED", orderIdForNotify, title, body, assignedShipperId);
        return new NotifyPayload(title, body, data);
      }
      default -> {
        return null;
      }
    }
  }

  private Map<String, String> buildDataMap(
      String eventType, UUID orderId, String title, String body, String extra) {
    Map<String, String> data = new HashMap<>();
    data.put("eventType", eventType);
    data.put("orderID", orderId != null ? orderId.toString() : "");
    data.put("title", title);
    data.put("message", body);
    if (extra != null) data.put("extra", extra);
    return data;
  }
}
