package com.pbl6.microservices.auth.service;

import com.pbl6.microservices.auth.dto.AuthResponse;
import com.pbl6.microservices.auth.entity.RefreshToken;
import com.pbl6.microservices.auth.entity.User;
import com.pbl6.microservices.auth.repository.RefreshTokenRepository;
import com.pbl6.microservices.auth.repository.UserRepository;
import com.pbl6.microservices.auth.util.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder;
    private final TokenBlacklistService blacklist;
    @Value("${JWT_REFRESH_EXP_MS:2592000000}")
    private long refreshMs;

    public AuthService(UserRepository userRepo, RefreshTokenRepository refreshRepo,
                       JwtUtils jwtUtils, TokenBlacklistService blacklist, PasswordEncoder encoder) {
        this.userRepo = userRepo; this.refreshRepo = refreshRepo;
        this.jwtUtils = jwtUtils; this.encoder = encoder;
        this.blacklist = blacklist;
    }

    // register
    @Transactional
    public User register(String phone, String rawPassword) {
        if (userRepo.existsByPhone(phone)) throw new RuntimeException("Phone already registered");
        User u = new User();
        u.setPhone(phone);
        u.setPassword(encoder.encode(rawPassword));
        u.setRoles("ROLE_USER");
        return userRepo.save(u);
    }

    // login
    @Transactional
    public AuthResponse login(String phone, String rawPassword) {
        User u = userRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!encoder.matches(rawPassword, u.getPassword())) throw new RuntimeException("Invalid credentials");

        String access = jwtUtils.generateAccessToken(u.getPhone(), u.getRoles());
        String refresh = jwtUtils.generateRefreshToken();
        Instant expiry = Instant.now().plusMillis(refreshMs);

        // tìm token cũ; nếu có thì cập nhật, nếu không thì tạo mới
        Optional<RefreshToken> existingOpt = refreshRepo.findByUser(u);
        if (existingOpt.isPresent()) {
            RefreshToken rt = existingOpt.get();
            rt.setToken(refresh);
            rt.setExpiryDate(expiry);
            refreshRepo.save(rt); // update
        } else {
            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(u);
            rt.setExpiryDate(expiry);
            refreshRepo.save(rt); // insert
        }

        return new AuthResponse(access, refresh, "Bearer");
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshRepo.delete(rt);
            throw new RuntimeException("Refresh token expired");
        }

        User u = rt.getUser();
        String newAccess = jwtUtils.generateAccessToken(u.getPhone(), u.getRoles());
        String newRefresh = jwtUtils.generateRefreshToken();

        rt.setToken(newRefresh);
        rt.setExpiryDate(Instant.now().plusMillis(refreshMs));
        refreshRepo.save(rt);

        return new AuthResponse(newAccess, newRefresh, "Bearer");
    }

    // refresh, logout unchanged except if you used username parameter before
//    @Transactional
//    public ResponseEntity<?> logout(String authHeader) {
//        String token = jwtUtils.resolveFromHeader(authHeader);
//        if (token == null || !jwtUtils.validate(token)) {
//            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or missing token"));
//        }
//
//        Date exp = jwtUtils.(token); // rename if your JwtUtils differs
//        long ttlMs = exp.getTime() - System.currentTimeMillis();
//        if (ttlMs > 0) {
//            blacklist.blacklist(token, Duration.ofMillis(ttlMs));
//        }
//        return ResponseEntity.ok(Map.of("message", "Logged out"));
//    }
}