package com.pbl6.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class FirebaseInitializer {

  private static final Logger log = LoggerFactory.getLogger(FirebaseInitializer.class);

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
        log.info("✅ Firebase has been initialized (auth-service)");
      } else {
        log.info("Firebase already initialized (auth-service), skipping re-init");
      }
    } catch (Exception e) {
      log.error("❌ Failed to initialize Firebase (auth-service)", e);
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
