package com.pbl6.order.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

class FirebaseConnectionTest {

  @BeforeEach
  void cleanBefore() {
    FirebaseApp.getApps().forEach(FirebaseApp::delete);
  }

  @AfterEach
  void cleanAfter() {
    FirebaseApp.getApps().forEach(FirebaseApp::delete);
  }

  @Test
  void firebaseAppInitializesWithProvidedCredentials() {
    String encoded = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");

    assertThat(StringUtils.hasText(encoded))
        .as(
            "FIREBASE_SERVICE_ACCOUNT_BASE64 environment variable must be provided for build-time verification")
        .isTrue();

    FirebaseInitializer initializer = new FirebaseInitializer();
    ReflectionTestUtils.setField(initializer, "firebaseServiceAccountBase64", encoded);
    initializer.init();

    assertThat(FirebaseApp.getApps())
        .as("FirebaseApp should be initialized when credentials are valid")
        .isNotEmpty();
  }
}
