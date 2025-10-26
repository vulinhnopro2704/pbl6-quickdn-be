package com.pbl6.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return """
            <html>
              <head><title>Gateway</title></head>
              <body style='font-family:sans-serif;text-align:center;margin-top:20%'>
                <h1>Hello, I'm Gateway</h1>
              </body>
            </html>
            """;
    }

    @GetMapping("/test")
    public String test() {
       return """
            <html>
              <head><title>Gateway</title></head>
              <body style='font-family:sans-serif;text-align:center;margin-top:20%'>
                <h1>Hello, I'm Gateway Test Path</h1>
              </body>
            </html>
            """;
    }
}