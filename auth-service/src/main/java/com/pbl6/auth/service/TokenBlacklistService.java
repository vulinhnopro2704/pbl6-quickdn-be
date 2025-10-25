package com.pbl6.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private static final String PREFIX = "jwt:blacklist:";
    private final StringRedisTemplate redis;

    public TokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void blacklist(String token, Duration ttl) {
        if (token == null || ttl == null || ttl.isNegative() || ttl.isZero()) return;
        redis.opsForValue().set(PREFIX + token, "1", ttl);
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Boolean exists = redis.hasKey(PREFIX + token);
        return exists != null && exists;
    }
}
