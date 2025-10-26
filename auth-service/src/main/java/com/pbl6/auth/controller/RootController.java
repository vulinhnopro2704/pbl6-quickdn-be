package com.pbl6.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/test")
    public String home() {
        return """
            <html>
              <head><title>Gateway</title></head>
              <body style='font-family:sans-serif;text-align:center;margin-top:20%'>
                <h1>Hello, I'm Auth Service</h1>
              </body>
            </html>
            """;
    }
}
