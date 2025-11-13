package com.pbl6.order.controller;

import com.pbl6.order.dto.CreateOrderRequest;
import com.pbl6.order.dto.CreateOrderResponse;
import com.pbl6.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest req) {
        var resp = orderService.createOrder(req);
        return ResponseEntity.ok(resp);
    }

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