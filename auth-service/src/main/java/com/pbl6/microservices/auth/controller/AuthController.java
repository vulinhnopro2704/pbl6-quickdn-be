package com.pbl6.microservices.auth.controller;

import com.pbl6.microservices.auth.dto.AuthResponse;
import com.pbl6.microservices.auth.dto.LoginRequest;
import com.pbl6.microservices.auth.dto.RegisterRequest;
import com.pbl6.microservices.auth.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){ this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        var u = authService.register(req.phone(), req.password());
        return ResponseEntity.ok(Map.of("id", u.getId(), "phone", u.getPhone()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req){
        return ResponseEntity.ok(authService.login(req.phone(), req.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String,String> body){
        return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
//        return authService.logout(authHeader);
//    }

    private String resolveFromHeader(String header) {
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }

    @GetMapping("/test")
    public String test(){
        return "Hello Auth Service";
    }


}