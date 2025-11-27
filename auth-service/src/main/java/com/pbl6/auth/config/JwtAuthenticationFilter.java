package com.pbl6.auth.config;


import com.pbl6.auth.repository.UserRepository;
import com.pbl6.auth.service.TokenBlacklistService;
import com.pbl6.auth.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService blacklist;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, TokenBlacklistService blacklist, UserRepository userRepo) {
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.blacklist = blacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
            String token = jwtUtils.resolveFromHeader(authHeader); // giữ cách bạn resolve "Bearer ..."

            if (StringUtils.hasText(token) && !blacklist.isBlacklisted(token) && jwtUtils.validate(token)) {

                // Lấy phone từ token
                String phone = jwtUtils.getPhoneFromToken(token);
                String subject = jwtUtils.getSubject(token);

                // Lấy role list từ token (sử dụng helper mới trong jwtUtils)
                List<String> roleNames = jwtUtils.getRolesFromToken(token);

                // Chuyển thành GrantedAuthority
                List<SimpleGrantedAuthority> authorities = roleNames.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // (Tùy chọn) kiểm tra user còn tồn tại và active/enable
                // Nếu bạn muốn đảm bảo account chưa bị khoá, hãy fetch user và kiểm tra.
                userRepo.findByPhone(phone).ifPresent(u -> {
                    if (!u.isEnabled() || !u.isActive()) {
                        // nếu account không active, không set auth
                        return;
                    }
                    // Nếu muốn chắc chắn quyền lấy từ DB thay vì token, có thể override authorities ở đây:
                    // authorities = u.getRoles().stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();
                    var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });

                // Nếu user không tồn tại nhưng bạn vẫn muốn set auth dựa vào token:
                // Optional<User> opt = userRepo.findByPhone(phone);
                // if (opt.isEmpty()) {
                //     var auth = new UsernamePasswordAuthenticationToken(phone, null, authorities);
                //     SecurityContextHolder.getContext().setAuthentication(auth);
                // }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // token invalid/expired/etc -> không set auth
            SecurityContextHolder.clearContext();
            // bạn có thể log ở đây
        } catch (Exception ex) {
            // phòng lỗi bất ngờ
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}