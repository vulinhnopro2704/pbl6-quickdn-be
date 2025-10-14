package com.pbl6.microservices.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")

public class OrderController {
    @GetMapping("/test")
    public String test() {
        return "Hello Order Service";
    }
}
