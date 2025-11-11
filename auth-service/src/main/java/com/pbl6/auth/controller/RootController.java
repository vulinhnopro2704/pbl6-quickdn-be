package com.pbl6.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health Check", description = "Service health and status verification endpoints")
public class RootController {

    @GetMapping("/test")
    @Operation(
        summary = "Root health check",
        description = "Verify that the Auth Service is running and accessible at the root context path"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy and running",
            content = @Content(
                mediaType = "text/html",
                examples = @ExampleObject(
                    name = "HTML Response",
                    value = """
                        <html>
                          <head><title>Gateway</title></head>
                          <body style='font-family:sans-serif;text-align:center;margin-top:20%'>
                            <h1>Hello, I'm Auth Service</h1>
                          </body>
                        </html>
                        """
                )
            )
        )
    })
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
