package com.pbl6.auth.service;

import com.pbl6.auth.dto.AuthResponse;
import com.pbl6.auth.entity.RefreshToken;
import com.pbl6.auth.entity.Role;
import com.pbl6.auth.entity.User;
import com.pbl6.auth.repository.RefreshTokenRepository;
import com.pbl6.auth.repository.UserRepository;
import com.pbl6.auth.util.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
    public User register(String phone, String rawPassword, String fullName) {
        if (userRepo.existsByPhone(phone)) {
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(encoder.encode(rawPassword));
        user.setFullName(fullName);
        user.getRoles().add(Role.ROLE_USER); // Mặc định cho user mới

        return userRepo.save(user);
    }


    // login
    @Transactional
    public AuthResponse login(String phone, String rawPassword) {
        User u = userRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(rawPassword, u.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!u.isEnabled() || !u.isActive()) {
            throw new RuntimeException("Account not active");
        }

        // Gọi trực tiếp, không cần convert ở đây
        String access = jwtUtils.generateAccessToken(u.getPhone(), u.getRoles());
        String refresh = jwtUtils.generateRefreshToken();
        Instant expiry = Instant.now().plusMillis(refreshMs);

        Optional<RefreshToken> existingOpt = refreshRepo.findByUser(u);
        if (existingOpt.isPresent()) {
            RefreshToken rt = existingOpt.get();
            rt.setToken(refresh);
            rt.setExpiryDate(expiry);
            refreshRepo.save(rt);
        } else {
            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(u);
            rt.setExpiryDate(expiry);
            refreshRepo.save(rt);
        }

        return new AuthResponse(access, refresh, "Bearer");
    }



    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // kiểm tra expiry null hoặc expired
        Instant now = Instant.now();
        if (rt.getExpiryDate() == null || rt.getExpiryDate().isBefore(now)) {
            refreshRepo.delete(rt); // remove expired token
            throw new BadCredentialsException("Refresh token expired");
        }

        User u = rt.getUser();
        if (u == null) {
            // dữ liệu không hợp lệ: xóa refresh token để an toàn
            refreshRepo.delete(rt);
            throw new IllegalStateException("Refresh token has no associated user");
        }

        // kiểm tra user còn active/enable
        if (!u.isEnabled() || !u.isActive()) {
            // Có thể xoá token để an toàn
            refreshRepo.delete(rt);
            throw new AccessDeniedException("User account is not active");
        }

        // Tạo access mới (giữ jwtUtils nhận Collection<Role>)
        String newAccess = jwtUtils.generateAccessToken(u.getPhone(), u.getRoles());
        String newRefresh = jwtUtils.generateRefreshToken();

        // Rotate token: cập nhật bản ghi với token mới và expiry mới
        rt.setToken(newRefresh);
        rt.setExpiryDate(now.plusMillis(refreshMs));
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