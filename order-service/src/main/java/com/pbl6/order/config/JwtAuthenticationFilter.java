package com.pbl6.order.config;

import com.pbl6.order.util.JwtUtils;
import io.jsonwebtoken.Claims;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
            String token = jwtUtils.resolveFromHeader(authHeader); // hoặc header.substring(7) nếu bạn muốn

            if (StringUtils.hasText(token) && jwtUtils.validate(token)) {

                // Lấy subject (phone hoặc userId tuỳ bạn set khi tạo token)
                String subject = jwtUtils.getPhoneFromToken(token); // hoặc getSubject(token)

                // Lấy roles từ token (cố gắng trả về List<String>)
                List<String> roleNames = jwtUtils.getRolesFromToken(token); // <- implement helper trong jwtUtils

                // Fallback: nếu jwtUtils chưa có helper, parse generic:
                // Claims claims = jwtUtils.getAllClaims(token);
                // Object obj = claims.get("roles");
                // if (obj instanceof List) { ... } else if (obj instanceof String) { ... }

                List<SimpleGrantedAuthority> authorities = roleNames.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            // token invalid/expired -> clear context, không ném exception ra client ở đây
            SecurityContextHolder.clearContext();
            // optional: log.debug("Invalid JWT: {}", ex.getMessage());
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            // optional: log.error("Unexpected error in JWT filter", ex);
        }

        chain.doFilter(req, res);
    }


    // nếu bạn muốn skip filtering cho một vài path (ví dụ /health), override shouldNotFilter...
}