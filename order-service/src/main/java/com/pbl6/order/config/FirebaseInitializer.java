package com.pbl6.order.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseInitializer {

  @PostConstruct
  public void init() {
    try {
      InputStream serviceAccount =
          getClass()
              .getClassLoader()
              .getResourceAsStream(
                  "serviceAccountKey.json");

      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
      }

      System.out.println("✅ Firebase has been initialized");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
