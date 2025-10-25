package com.pbl6.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_EXP_MS:300000}")
    private long accessMs;

    @Value("${JWT_REFRESH_EXP_MS:2592000000}")
    private long refreshMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String phone, String roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(phone)          // subject = phone now
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // getPhoneFromToken (helper)
    public String getPhoneFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public String getSubject(String token) {
        return getAllClaims(token).getSubject();
    }

    public String getJti(String token) {
        return getAllClaims(token).getId();
    }

    public long getRemainingSeconds(String token) {
        Date exp = getAllClaims(token).getExpiration();
        long sec = (exp.getTime() - System.currentTimeMillis()) / 1000L;
        return Math.max(sec, 0L);
    }

    public String resolveFromHeader(String header) {
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }
}
