package com.pbl6.goongmap.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goongmap")
public class GoongMapController {
    @GetMapping("/test")
    public String test() {
        return "Hello GoongMap Service";
    }
}
