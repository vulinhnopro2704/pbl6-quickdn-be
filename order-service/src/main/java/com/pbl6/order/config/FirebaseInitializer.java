package com.pbl6.order.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class FirebaseInitializer {

  @Value("${firebase.service-account-base64:}")
  private String firebaseServiceAccountBase64;

  @PostConstruct
  public void init() {
    try (InputStream serviceAccount = resolveServiceAccountStream()) {
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

  private InputStream resolveServiceAccountStream() {
    if (StringUtils.hasText(firebaseServiceAccountBase64)) {
      byte[] decoded = Base64.getDecoder().decode(firebaseServiceAccountBase64);
      return new ByteArrayInputStream(decoded);
    }

    InputStream fallbackStream =
        getClass()
            .getClassLoader()
            .getResourceAsStream(
                "serviceAccountKey.json");

    if (fallbackStream == null) {
      throw new IllegalStateException(
          "Firebase credentials are not configured. Set firebase.service-account-base64 or include serviceAccountKey.json in resources.");
    }

    return fallbackStream;
  }
}
