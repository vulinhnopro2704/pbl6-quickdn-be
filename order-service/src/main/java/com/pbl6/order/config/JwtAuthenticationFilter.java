package com.pbl6.order.config;

import com.pbl6.order.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;

  public JwtAuthenticationFilter(JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    try {
      String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
      String token = jwtUtils.resolveFromHeader(authHeader);

      if (StringUtils.hasText(token)) {
        log.debug("JWT token found in request to: {}", req.getRequestURI());

        if (jwtUtils.validate(token)) {
          // Lấy subject (phone hoặc userId tuỳ bạn set khi tạo token)
          String subject = jwtUtils.getSubject(token);

          // Lấy roles từ token
          List<String> roleNames = jwtUtils.getRolesFromToken(token);

          List<SimpleGrantedAuthority> authorities =
              roleNames.stream()
                  .filter(Objects::nonNull)
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .map(SimpleGrantedAuthority::new)
                  .collect(Collectors.toList());

          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(subject, null, authorities);

          SecurityContextHolder.getContext().setAuthentication(auth);
          log.debug("Successfully authenticated user: {} with roles: {}", subject, roleNames);
        } else {
          log.warn("Invalid JWT token for request to: {}", req.getRequestURI());
        }
      } else {
        log.debug("No JWT token found in request to: {}", req.getRequestURI());
      }
    } catch (io.jsonwebtoken.JwtException ex) {
      // token invalid/expired
      SecurityContextHolder.clearContext();
      log.warn("JWT validation failed for request to {}: {}", req.getRequestURI(), ex.getMessage());
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
      log.error(
          "Unexpected error in JWT filter for request to {}: {}",
          req.getRequestURI(),
          ex.getMessage(),
          ex);
    }

    chain.doFilter(req, res);
  }

  // nếu bạn muốn skip filtering cho một vài path (ví dụ /health), override shouldNotFilter...

}
