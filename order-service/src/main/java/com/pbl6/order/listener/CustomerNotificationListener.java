package com.pbl6.order.listener;

import com.pbl6.order.event.OrderAssignedEvent;
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

@Component
public class CustomerNotificationListener {

  private static final Logger log = LoggerFactory.getLogger(CustomerNotificationListener.class);

  // key pattern (để giống code cũ của bạn)
  private static final String USER_FCM_TOKEN = "user:%s:fcm"; // sửa theo key actual của bạn

  private final RedisTemplate<String, String> redisTemplate;
  private final FirebaseMessagingService firebaseMessagingService;
  private final ExecutorService pushExecutor;

  public CustomerNotificationListener(
      RedisTemplate<String, String> redisTemplate,
      FirebaseMessagingService firebaseMessagingService,
      ExecutorService pushExecutor) {
    this.redisTemplate = redisTemplate;
    this.firebaseMessagingService = firebaseMessagingService;
    this.pushExecutor = pushExecutor;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderAssigned(OrderAssignedEvent ev) {
    UUID creatorId = ev.getCreatorId();
    String orderIdStr = ev.getOrderId().toString();
    String title = "Đơn hàng của bạn đã có tài xế";
    String body = "Vui lòng kiểm tra trạng thái đơn hàng của bạn";

    try {
      pushExecutor.submit(
          () -> {
            try {
              String userFcmTokenKey = String.format(USER_FCM_TOKEN, creatorId);
              String fcmToken = redisTemplate.opsForValue().get(userFcmTokenKey);
              if (fcmToken != null && !fcmToken.isEmpty()) {
                Map<String, String> data = new HashMap<>();
                data.put("status", "🚗 Đơn hàng của bạn đã có tài xế");
                data.put("orderID", orderIdStr);
                data.put("message", body);

                firebaseMessagingService.sendNotificationWithData(fcmToken, title, body, data);
                log.info(
                    "Pushed order-assigned notification for order={} to user={}",
                    orderIdStr,
                    creatorId);
              } else {
                log.info("No FCM token found for user={}, key={}", creatorId, userFcmTokenKey);
              }
            } catch (Exception ex) {
              log.error(
                  "Error when sending push for order={} to user={}", orderIdStr, creatorId, ex);
            }
          });
    } catch (Exception submitEx) {
      // fallback: best-effort synchronous send (ngăn app crash)
      log.warn(
          "Push executor rejected or failed, running fallback send for order={}",
          orderIdStr,
          submitEx);
      try {
        String userFcmTokenKey = String.format(USER_FCM_TOKEN, creatorId);
        String fcmToken = redisTemplate.opsForValue().get(userFcmTokenKey);
        if (fcmToken != null && !fcmToken.isEmpty()) {
          Map<String, String> data = new HashMap<>();
          data.put("status", "🚗 Đơn hàng của bạn đã có tài xế");
          data.put("orderID", orderIdStr);
          data.put("message", body);
          firebaseMessagingService.sendNotificationWithData(fcmToken, title, body, data);
        }
      } catch (Exception ex) {
        log.error("Fallback send also failed for order={} user={}", orderIdStr, creatorId, ex);
      }
    }
  }
}
