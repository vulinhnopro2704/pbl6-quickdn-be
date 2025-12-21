package com.pbl6.order.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.pbl6.order.constant.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static com.pbl6.order.constant.RedisKeyConstants.*;

@Slf4j
@Service
public class ShipperPushService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ExecutorService pushExecutor;
  private final int candidateLimit = 100; // M (số candidate lấy ban đầu)
  private final long perBatchTimeoutMs = 8000; // T (chờ giữa các batch, ví dụ 8000 ms)

  public ShipperPushService(
      RedisTemplate<String, String> redisTemplate, ExecutorService pushExecutor) {
    this.redisTemplate = redisTemplate;
    this.pushExecutor = pushExecutor;
  }

  /**
   * Public API: gọi hàm này khi có order mới. Hàm non-blocking: submit task vào executor rồi return
   * ngay.
   *
   * @param orderId order id
   * @param longitude kinh độ (double)
   * @param latitude vĩ độ (double)
   * @param k số shipper muốn push trong mỗi batch (top-K)
   */
  public void pushToNearestShippersAsync(UUID orderId, double longitude, double latitude, int k) {
    // Submit task bất đồng bộ - trả về ngay
    pushExecutor.submit(() -> pushToNearestShippersWorker(orderId, longitude, latitude, k));
  }

  /**
   * Worker thực hiện tìm shipper và push theo batch. Chạy trong thread pool (không block caller).
   */
  private void pushToNearestShippersWorker(UUID orderId, double longitude, double latitude, int k) {
    String assigneeKey = String.format(ORDER_ASSIGNEE_KEY_PATTERN, orderId.toString());

    try {
      // 1) Lấy danh sách candidate từ GEO (gần -> xa)
      // radius: 10 km (có thể thay). Lấy tối đa candidateLimit sau khi filter online.
      Circle circle =
          new Circle(new Point(longitude, latitude), new Distance(50, Metrics.KILOMETERS));
      GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
          redisTemplate.opsForGeo().radius(DRIVERS_GEO_KEY, circle);

      List<String> candidateIds = new ArrayList<>();
      if (geoResults != null) {
        candidateIds =
            geoResults.getContent().stream()
                .map(g -> g.getContent().getName()) // shipperId as String
                .filter(
                    this
                        ::isDriverAvailable) // lọc online/available (kiểm tra FCM token hoặc
                                             // status)
                .limit(candidateLimit)
                .toList();
      }

      if (candidateIds.isEmpty()) {
        log.debug("No available drivers found for order {}", orderId);
        return;
      }

      // 2) Batch push: mỗi lần gửi top-k shipper tiếp theo, chờ perBatchTimeoutMs để xem có ai nhận
      // không
      int idx = 0;
      final int total = candidateIds.size();

      while (idx < total) {
        // Trước khi gửi batch kiểm tra order đã có assignee chưa
        if (isOrderAssigned(assigneeKey)) {
          // có người nhận rồi -> dừng
          return;
        }

        int end = Math.min(idx + k, total);
        List<String> batch = candidateIds.subList(idx, end);

        // Gửi notification không đồng bộ cho từng driver trong batch
        for (String driverId : batch) {
          // kiểm tra lại order trước khi gọi push để giảm gửi thừa
          if (isOrderAssigned(assigneeKey)) {
            return;
          }

          // Nếu sendPushToDriver blocking thì cân nhắc submit mỗi push vào executor khác.
          sendPushToDriver(driverId, orderId.toString());
        }

        // Sau khi gửi batch, chờ 0..perBatchTimeoutMs nhưng check orderAssigned sớm (polling nhỏ)
        long waited = 0;
        final long pollInterval = 200; // check every 200ms để dừng sớm nếu có assignee
        while (waited < perBatchTimeoutMs) {
          if (isOrderAssigned(assigneeKey)) {
            return; // có shipper nhận -> dừng ngay
          }
          try {
            Thread.sleep(Math.min(pollInterval, perBatchTimeoutMs - waited));
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
          waited += pollInterval;
        }

        // nếu hết timeout mà chưa ai nhận -> tiếp batch tiếp theo
        idx = end;
      }
      log.debug("No available drivers found for order {}", orderId);
      // Hết danh sách candidate
    } catch (Exception ex) {
      // log error nhưng không throw (worker), tránh crash cả pool
      // logger.warn("Push worker failed for order {}: {}", orderId, ex.getMessage());
    }
  }

  /**
   * Kiểm tra order đã có assignee hay chưa. Trả về true nếu order đã được assign
   * (order:{id}:assignee tồn tại / có value).
   */
  private boolean isOrderAssigned(String assigneeKey) {
    try {
        return redisTemplate.hasKey(assigneeKey);
    } catch (Exception ex) {
      // trong trường hợp redis lỗi, tránh false negative -> log rồi assume not assigned (tùy
      // policy)
      return false;
    }
  }

  /**
   * Kiểm tra driver có available (ví dụ có FCM token) hay không. Dùng cùng key pattern với hàm
   * updateLocation của bạn: DRIVER_FCM_TOKEN
   */
  private boolean isDriverAvailable(String driverId) {
    try {
      String tokenKey = String.format(DRIVER_FCM_TOKEN, driverId);
      String isDelivering = String.format(DRIVER_DELIVERING_ORDER_KEY, driverId);
        return redisTemplate.hasKey(tokenKey) && !redisTemplate.hasKey(isDelivering);
    } catch (Exception ex) {
      return false;
    }
  }

  /** Gửi push notification tới driver. Dùng sendAsync của Firebase để không block. */
  private void sendPushToDriver(String driverId, String orderId) {
    try {
      System.out.println("Candidates driver: " + driverId + " for order " + orderId);
      String tokenKey = String.format(DRIVER_FCM_TOKEN, driverId);
      String fcmToken = redisTemplate.opsForValue().get(tokenKey);
      if (fcmToken == null || fcmToken.isEmpty()) return;

      Map<String, String> data =
          Map.of(
              "status", "📦 Bạn có đơn hàng mới!",
              "orderId", orderId,
              "message", "Vui lòng kiểm tra để nhận.");

      Notification notification =
          Notification.builder()
              .setTitle("📦 Bạn có đơn hàng mới!")
              .setBody("Mời xem chi tiết và nhận đơn.")
              .build();

      Message message =
          Message.builder()
              .setToken(fcmToken)
              .setNotification(notification)
              .putAllData(data)
              .build();

      // sendAsync để không block worker thread
      FirebaseMessaging.getInstance().sendAsync(message);
    } catch (Exception ex) {
      // log error nếu cần
      // logger.error("Failed to send push to driver {} for order {}: {}", driverId, orderId,
      // ex.getMessage());
    }
  }
}
