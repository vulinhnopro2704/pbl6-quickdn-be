package com.pbl6.order.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthLogger {

  private final RedisTemplate<String, String> redisTemplate;

  @PostConstruct
  public void checkRedisPing() {
    try {
      System.out.println("👉 Redis host: " + System.getenv("REDIS_HOST"));
      System.out.println("👉 Redis port: " + System.getenv("REDIS_PORT"));
      assert redisTemplate.getConnectionFactory() != null;
      String pong = redisTemplate.getConnectionFactory().getConnection().ping();
      System.out.println("✅ Redis ping: " + pong);
    } catch (Exception e) {
      System.err.println("❌ Redis connection failed: " + e.getMessage());
    }
  }
}
