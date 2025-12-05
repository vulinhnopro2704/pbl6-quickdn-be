package com.pbl6.order.service;

import com.google.firebase.messaging.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {

  public String sendToToken(String fcmToken, String title, String body)
      throws ExecutionException, InterruptedException {
    Notification notification = Notification.builder().setTitle(title).setBody(body).build();

    Message message = Message.builder().setToken(fcmToken).setNotification(notification).build();

    String response = FirebaseMessaging.getInstance().sendAsync(message).get();
    return response;
  }

    public String sendNotificationWithData(String fcmToken, String title, String body, Map<String, String> data)
            throws Exception {

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(data)
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

}
