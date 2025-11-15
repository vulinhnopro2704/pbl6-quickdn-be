package com.pbl6.order.controller;

import com.pbl6.order.dto.CreateOrderRequest;
import com.pbl6.order.dto.CreateOrderResponse;
import com.pbl6.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a new order", description = "Create an order containing pickup address, packages and options (cod, fragile, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateOrderResponse.class), examples = @ExampleObject(value = "{\"orderId\": \"550e8400-e29b-41d4-a716-446655440000\", \"totalAmount\": 120000.0, \"currency\": \"VND\", \"status\": \"PENDING\"}"))),
            @ApiResponse(responseCode = "400", description = "Bad Request - validation failed or invalid input", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"Validation failed\", \"status\": 400}"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred\", \"status\": 500}")))
    })
    public ResponseEntity<CreateOrderResponse> createOrder(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Order creation payload",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateOrderRequest.class),
                examples = @ExampleObject(value = "{\"pickupAddress\":{\"detail\":\"123 Le Loi\",\"name\":\"Store A\",\"phone\":\"0912345678\"},\"packages\":[{\"size\":\"M\",\"weightKg\":1.2,\"receiverAddress\":{\"detail\":\"456 Nguyen Trai\",\"name\":\"Customer B\",\"phone\":\"0987654321\"},\"payerType\":\"SENDER\",\"category\":\"KHAC\"}],\"customerNote\":\"Leave at door\",\"fragile\":false}")
            )
        )
        @Valid @RequestBody CreateOrderRequest req) {
        var resp = orderService.createOrder(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Simple endpoint to verify that the order service is running")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Service is running", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Hello Order Service")))})
    public String test() {
        return "Hello Order Service";
    }

    @GetMapping
    @Operation(summary = "List orders for authenticated user", description = "Returns a short acknowledgement with the authenticated principal; implement paging in future")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "List returned")})
    public ResponseEntity<?> listOrders(Authentication authentication) {
        String subject = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of("msg", "Đã xác thực", "user", subject));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id", description = "Retrieve order metadata by id. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication authentication) {
        String subject = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of("orderId", id, "owner", subject));
    }
}