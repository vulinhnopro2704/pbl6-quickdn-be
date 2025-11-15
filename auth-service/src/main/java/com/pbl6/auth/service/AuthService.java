package com.pbl6.auth.service;

import com.pbl6.auth.dto.AuthResponse;
import com.pbl6.auth.entity.RefreshToken;
import com.pbl6.auth.entity.Role;
import com.pbl6.auth.entity.User;
import com.pbl6.auth.exception.BadRequestException;
import com.pbl6.auth.exception.UnauthorizedException;
import com.pbl6.auth.repository.RefreshTokenRepository;
import com.pbl6.auth.repository.UserRepository;
import com.pbl6.auth.util.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
            throw new BadRequestException("Số điện thoại đã tồn tại");
        }

        // Validate phone format (Vietnamese phone number: 10-11 digits starting with 0)
        if (phone == null || !phone.matches("^0\\d{9,10}$")) {
            throw new BadRequestException("Định dạng số điện thoại không hợp lệ");
        }

        // Validate password
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 8 ký tự");
        }

        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BadRequestException("Họ tên là bắt buộc");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(encoder.encode(rawPassword));
        user.setFullName(fullName);
        user.getRoles().add(Role.ROLE_USER);

        return userRepo.save(user);
    }


    // login
    @Transactional
    public AuthResponse login(String phone, String rawPassword) {
        // Validate input
        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Số điện thoại là bắt buộc");
        }
        
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new BadRequestException("Mật khẩu là bắt buộc");
        }

        User u = userRepo.findByPhone(phone)
                .orElseThrow(() -> new UnauthorizedException("Số điện thoại hoặc mật khẩu không hợp lệ"));

        if (!encoder.matches(rawPassword, u.getPassword())) {
            throw new UnauthorizedException("Số điện thoại hoặc mật khẩu không hợp lệ");
        }

        if (!u.isEnabled() || !u.isActive()) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt hoặc đã bị vô hiệu hóa");
        }

        // Gọi trực tiếp, không cần convert ở đây
        String access = jwtUtils.generateAccessToken(u.getId(), u.getPhone(), u.getRoles());
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
        // Validate input
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BadRequestException("Refresh token là bắt buộc");
        }

        RefreshToken rt = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token không hợp lệ"));

        // kiểm tra expiry null hoặc expired
        Instant now = Instant.now();
        if (rt.getExpiryDate() == null || rt.getExpiryDate().isBefore(now)) {
            refreshRepo.delete(rt); // remove expired token
            throw new UnauthorizedException("Refresh token đã hết hạn");
        }

        User u = rt.getUser();
        if (u == null) {
            // dữ liệu không hợp lệ: xóa refresh token để an toàn
            refreshRepo.delete(rt);
            throw new BadRequestException("Refresh token không có người dùng liên kết");
        }

        // kiểm tra user còn active/enable
        if (!u.isEnabled() || !u.isActive()) {
            // Có thể xoá token để an toàn
            refreshRepo.delete(rt);
            throw new UnauthorizedException("Tài khoản người dùng chưa được kích hoạt hoặc đã bị vô hiệu hóa");
        }

        // Tạo access mới (giữ jwtUtils nhận Collection<Role>)
        String newAccess = jwtUtils.generateAccessToken(u.getId(), u.getPhone(), u.getRoles());
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