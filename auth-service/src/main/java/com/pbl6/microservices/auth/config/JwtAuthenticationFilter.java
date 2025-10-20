package com.pbl6.microservices.auth.config;


import com.pbl6.microservices.auth.repository.UserRepository;
import com.pbl6.microservices.auth.service.TokenBlacklistService;
import com.pbl6.microservices.auth.util.JwtUtils;
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
        String token = jwtUtils.resolveFromHeader(req.getHeader(HttpHeaders.AUTHORIZATION));
        if (StringUtils.hasText(token) && !blacklist.isBlacklisted(token) && jwtUtils.validate(token)) {
            String phone = jwtUtils.getPhoneFromToken(token);
            userRepo.findByPhone(phone).ifPresent(u -> {
                var auth = new UsernamePasswordAuthenticationToken(phone, null,
                        Arrays.stream((u.getRoles()==null?"":u.getRoles()).split(","))
                                .filter(s->!s.isBlank()).map(SimpleGrantedAuthority::new).toList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(req, res);
    }
}