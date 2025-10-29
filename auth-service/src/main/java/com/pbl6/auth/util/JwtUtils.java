package com.pbl6.auth.util;

import com.pbl6.auth.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-exp-ms:300000}")
    private long accessMs;

    @Value("${jwt.refresh-exp-ms:2592000000}")
    private long refreshMs;

    private Key key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is not configured. Please set jwt.secret in application.yml");
        }
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String phone, Collection<Role> roles) {
        List<String> roleNames = roles.stream()
                .map(Role::name)
                .toList();

        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(phone)
                .claim("roles", roleNames)
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
