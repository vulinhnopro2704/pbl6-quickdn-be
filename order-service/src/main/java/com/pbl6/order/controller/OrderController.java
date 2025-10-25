package com.pbl6.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order")

public class OrderController {
    @GetMapping("/test")
    public String test() {
        return "Hello Order Service";
    }
    // GET /api/orders -> require token
    @GetMapping
    public ResponseEntity<?> listOrders(Authentication authentication) {
        // authentication.getName() chứa subject từ token (ví dụ: phone)
        String subject = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of("msg", "Đã xác thực", "user", subject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication authentication) {
        String subject = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of("orderId", id, "owner", subject));
    }
}
