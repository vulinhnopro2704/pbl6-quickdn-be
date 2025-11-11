package com.pbl6.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerController {

    @GetMapping("/swagger")
    public String swaggerIndex(Model model) {
        return "swagger-index";
    }
}
