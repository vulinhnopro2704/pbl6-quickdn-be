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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
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
    @Operation(
            summary = "List orders for authenticated user",
            description = "Returns list of orders belonging to the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List returned",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value =
                        "{\n" +
                        "  \"orders\": [\n" +
                        "    {\n" +
                        "      \"id\": \"4be9d037-a453-4c37-9f7a-3d4483306622\",\n" +
                        "      \"totalAmount\": 72.00,\n" +
                        "      \"status\": \"PENDING\",\n" +
                        "      \"createdAt\": \"2025-11-20T21:28:22.711603\",\n" +
                        "      \"scheduledAt\": null\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": \"1c982a5d-7051-47b9-924d-2931777e4ae1\",\n" +
                        "      \"totalAmount\": 72.00,\n" +
                        "      \"status\": \"PENDING\",\n" +
                        "      \"createdAt\": \"2025-11-19T23:28:00.989874\",\n" +
                        "      \"scheduledAt\": null\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": \"9f4fc8e6-9f47-4eaf-99ac-a987ed4d7208\",\n" +
                        "      \"totalAmount\": 36.00,\n" +
                        "      \"status\": \"PENDING\",\n" +
                        "      \"createdAt\": \"2025-11-19T08:32:44.705027\",\n" +
                        "      \"scheduledAt\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
                )
            )
        )
    })
    public ResponseEntity<?> listOrders(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        var resp = orderService.getMyOrders(userId);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get order by id", description = "Retrieve order metadata by id. Requires authentication.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Order found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value =
                    "{\n" +
                        "  \"id\": \"fe28d8fc-d16d-4d6e-a79b-0fbb7ef77b67\",\n" +
                        "  \"totalAmount\": 72.00,\n" +
                        "  \"status\": \"PENDING\",\n" +
                        "  \"createdAt\": \"2025-11-19T07:34:46.98003\",\n" +
                        "  \"scheduledAt\": \"2025-11-05T09:30:00\",\n" +
                        "  \"pickupAddress\": {\n" +
                        "    \"detail\": \"123 Nguyễn Văn Linh, Đà Nẵng\",\n" +
                        "    \"name\": \"Nguyễn Văn Tâm\",\n" +
                        "    \"phone\": \"0905123456\",\n" +
                        "    \"latitude\": 16.0471,\n" +
                        "    \"longitude\": 108.2068\n" +
                        "  },\n" +
                        "  \"packages\": [\n" +
                        "    {\n" +
                        "      \"id\": \"4a29ec73-211a-4385-a8f3-9747bf225f18\",\n" +
                        "      \"weightKg\": 2.500,\n" +
                        "      \"packageSize\": \"M\",\n" +
                        "      \"deliveryFee\": 36.00,\n" +
                        "      \"codFee\": 150000.00,\n" +
                        "      \"payerType\": \"SENDER\",\n" +
                        "      \"category\": \"THOI_TRANG\",\n" +
                        "      \"description\": \"Áo thun nam\",\n" +
                        "      \"dropoffAddress\": {\n" +
                        "        \"detail\": \"45 Lê Duẩn, Đà Nẵng\",\n" +
                        "        \"name\": \"Trần Thị A\",\n" +
                        "        \"phone\": \"0987654321\",\n" +
                        "        \"latitude\": 16.0702,\n" +
                        "        \"longitude\": 108.2203\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": \"e00d5a1c-3df6-46a1-ab52-26b950bf1124\",\n" +
                        "      \"weightKg\": 4.000,\n" +
                        "      \"packageSize\": \"L\",\n" +
                        "      \"deliveryFee\": 36.00,\n" +
                        "      \"codFee\": 0.00,\n" +
                        "      \"payerType\": \"RECEIVER\",\n" +
                        "      \"category\": \"QUA_TANG\",\n" +
                        "      \"description\": \"Hộp quà Tết\",\n" +
                        "      \"dropoffAddress\": {\n" +
                        "        \"detail\": \"12 Nguyễn Tri Phương, Đà Nẵng\",\n" +
                        "        \"name\": \"Phạm Văn B\",\n" +
                        "        \"phone\": \"0934567890\",\n" +
                        "        \"latitude\": 16.0653,\n" +
                        "        \"longitude\": 108.2135\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
                )
            )
        ),
    @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<?> getOrderById(
            @PathVariable UUID id,
            Authentication auth
    ) {
        UUID userId = UUID.fromString(auth.getName());
        var resp = orderService.getOrderById(userId, id);
        return ResponseEntity.ok(resp);
    }

}