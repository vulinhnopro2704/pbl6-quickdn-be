package com.pbl6.auth.controller;

import com.pbl6.auth.dto.AuthResponse;
import com.pbl6.auth.dto.LoginRequest;
import com.pbl6.auth.dto.RegisterRequest;
import com.pbl6.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Authentication", description = "Authentication and Authorization APIs for user management")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){ this.authService = authService; }

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account with phone number, password, and full name. Phone number must be unique and in Vietnamese format (10-11 digits starting with 0)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Successful Registration",
                    value = """
                        {
                          "id": "550e8400-e29b-41d4-a716-446655440000",
                          "phone": "0912345678",
                          "fullName": "Nguyen Van A"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid input data or phone number already exists",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Phone Already Exists",
                        value = """
                            {
                              "error": "Bad Request",
                              "message": "Phone number already exists",
                              "status": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Invalid Phone Format",
                        value = """
                            {
                              "error": "Bad Request",
                              "message": "Invalid phone number format",
                              "status": 400
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                        {
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred",
                          "status": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<?> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "Registration Example",
                    value = """
                        {
                          "phone": "0912345678",
                          "password": "SecurePass123!",
                          "fullName": "Nguyen Van A"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody RegisterRequest req
    ) {
        var user = authService.register(req.phone(), req.password(), req.fullName());
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "phone", user.getPhone(),
                "fullName", user.getFullName()
        ));
    }


    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with phone number and password. Returns access token and refresh token for API authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful - Returns JWT tokens",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful Login",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYzMjU0MjJ9.4Adcj0vgH0y8yC3_Cx0eKT8fwpMeJf36POk6yJV_Qss",
                          "tokenType": "Bearer"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Credentials",
                    value = """
                        {
                          "error": "Unauthorized",
                          "message": "Invalid phone number or password",
                          "status": 401
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Missing or invalid input",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Input",
                    value = """
                        {
                          "error": "Bad Request",
                          "message": "Phone and password are required",
                          "status": 400
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                        {
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred",
                          "status": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "Login Example",
                    value = """
                        {
                          "phone": "0912345678",
                          "password": "SecurePass123!"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody LoginRequest req
    ){
        return ResponseEntity.ok(authService.login(req.phone(), req.password()));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generate a new access token using a valid refresh token. The refresh token must not be expired or blacklisted."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token successfully refreshed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful Refresh",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjQzMjIyfQ.NewAccessTokenHere",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYzMjU0MjJ9.4Adcj0vgH0y8yC3_Cx0eKT8fwpMeJf36POk6yJV_Qss",
                          "tokenType": "Bearer"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or expired refresh token",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Expired Token",
                        value = """
                            {
                              "error": "Unauthorized",
                              "message": "Refresh token has expired",
                              "status": 401
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Invalid Token",
                        value = """
                            {
                              "error": "Unauthorized",
                              "message": "Invalid refresh token",
                              "status": 401
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Missing refresh token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Missing Token",
                    value = """
                        {
                          "error": "Bad Request",
                          "message": "Refresh token is required",
                          "status": 400
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                        {
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred",
                          "status": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> refresh(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token request body",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Refresh Token Example",
                    value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicGhvbmUiOiIwOTEyMzQ1Njc4IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYzMjU0MjJ9.4Adcj0vgH0y8yC3_Cx0eKT8fwpMeJf36POk6yJV_Qss"
                        }
                        """
                )
            )
        )
        @RequestBody Map<String,String> body
    ){
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
    @Operation(
        summary = "Test endpoint",
        description = "Simple endpoint to verify that the authentication service is running properly"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is running",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "Hello Auth Service"
                )
            )
        )
    })
    public String test(){
        return "Hello Auth Service";
    }

    
}