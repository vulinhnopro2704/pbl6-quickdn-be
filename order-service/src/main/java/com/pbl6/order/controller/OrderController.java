package com.pbl6.order.controller;

import com.pbl6.order.dto.*;
import com.pbl6.order.dto.payment.PaymentSuccessRequest;
import com.pbl6.order.entity.PackageEntity;
import com.pbl6.order.exception.AppException;
import com.pbl6.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  @Operation(
      summary = "Create a new order",
      description =
          "Create an order containing pickup address, packages and options (cod, fragile, etc.)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateOrderResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                "{\"orderId\": \"550e8400-e29b-41d4-a716-446655440000\", \"totalAmount\": 120000.0, \"currency\": \"VND\", \"status\": \"PENDING\"}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - validation failed or invalid input",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"error\": \"Bad Request\", \"message\": \"Validation failed\", \"status\": 400}"))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred\", \"status\": 500}")))
      })
  public ResponseEntity<CreateOrderResponse> createOrder(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Order creation payload",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = CreateOrderRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"pickupAddress\":{\"detail\":\"123 Le Loi\",\"name\":\"Store A\",\"phone\":\"0912345678\"},\"packages\":[{\"size\":\"M\",\"weightKg\":1.2,\"receiverAddress\":{\"detail\":\"456 Nguyen Trai\",\"name\":\"Customer B\",\"phone\":\"0987654321\"},\"payerType\":\"SENDER\",\"category\":\"KHAC\"}],\"customerNote\":\"Leave at door\",\"fragile\":false}")))
          @Valid
          @RequestBody
          CreateOrderRequest req) {
    var resp = orderService.createOrder(req);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/test")
  @Operation(
      summary = "Test endpoint",
      description = "Simple endpoint to verify that the order service is running")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Service is running",
        content =
            @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Hello Order Service")))
  })
  public String test() {
    return "Hello Order Service";
  }

  @GetMapping
  @Operation(
      summary = "List orders (paged, searchable, sortable, filterable)",
      description =
          "page is 1-based. Default page=1, size=50. Supports q, fromDate, toDate, status, paymentMethod, sort.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "List returned",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            "{\n"
                                + "  \"orders\": [\n"
                                + "    {\n"
                                + "      \"id\": \"4be9d037-a453-4c37-9f7a-3d4483306622\",\n"
                                + "      \"totalAmount\": 72.00,\n"
                                + "      \"status\": \"PENDING\",\n"
                                + "      \"createdAt\": \"2025-11-20T21:28:22.711603\",\n"
                                + "      \"scheduledAt\": null\n"
                                + "    },\n"
                                + "    {\n"
                                + "      \"id\": \"1c982a5d-7051-47b9-924d-2931777e4ae1\",\n"
                                + "      \"totalAmount\": 72.00,\n"
                                + "      \"status\": \"PENDING\",\n"
                                + "      \"createdAt\": \"2025-11-19T23:28:00.989874\",\n"
                                + "      \"scheduledAt\": null\n"
                                + "    },\n"
                                + "    {\n"
                                + "      \"id\": \"9f4fc8e6-9f47-4eaf-99ac-a987ed4d7208\",\n"
                                + "      \"totalAmount\": 36.00,\n"
                                + "      \"status\": \"PENDING\",\n"
                                + "      \"createdAt\": \"2025-11-19T08:32:44.705027\",\n"
                                + "      \"scheduledAt\": null\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}")))
  })
  public ResponseEntity<Page<OrderDetailResponse>> listOrders(
      Authentication auth,
      @RequestParam(required = false) String role, // optional selector
      @RequestParam(required = false) String userId, // only for admin-role
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "50") Integer size,
      @RequestParam(required = false, defaultValue = "createdAt,desc") String sort) {
    // parse currentUserId
    UUID currentUserId = parsePrincipalId(auth);

    // collect roles user actually has
    Set<String> roles = extractRoles(auth);

    // If role param provided -> validate it's one of allowed values
    String roleToUse = null;
    if (role != null && !role.isBlank()) {
      // check user has this role
      if (!roles.contains(role)) {
        throw AppException.forbidden("Bạn không có role được yêu cầu: " + role);
      }
      roleToUse = role;
    } else {
      // no role param: derive default behavior
      // prefer admin if user has it, else use union of roles (handled in service)
      if (roles.contains("ADMIN")) {
        roleToUse = "ADMIN";
      } else {
        roleToUse = "USER"; // default to USER
      }
    }

    // parse pageable/sort
    int pageIndex = (page == null || page < 1) ? 0 : page - 1;
    Sort sortObj = buildSortFromParam(sort); // helper function similar to previous examples
    Pageable pageable = PageRequest.of(pageIndex, size, sortObj);

    // parse filterUserId only if present and will be authorized in service
    UUID filterUserId = null;
    if (userId != null && !userId.isBlank()) {
      try {
        filterUserId = UUID.fromString(userId);
      } catch (Exception ex) {
        throw com.pbl6.order.exception.AppException.badRequest("userId không hợp lệ");
      }
    }

    Page<OrderDetailResponse> resp =
        orderService.getOrdersByRoleSelector(
            currentUserId,
            roles,
            roleToUse,
            filterUserId,
            q,
            status,
            paymentMethod,
            fromDate,
            toDate,
            pageable);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get order by id",
      description = "Retrieve order metadata by id. Requires authentication.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Order found",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            "{\n"
                                + "  \"id\": \"fe28d8fc-d16d-4d6e-a79b-0fbb7ef77b67\",\n"
                                + "  \"totalAmount\": 72.00,\n"
                                + "  \"status\": \"PENDING\",\n"
                                + "  \"createdAt\": \"2025-11-19T07:34:46.98003\",\n"
                                + "  \"scheduledAt\": \"2025-11-05T09:30:00\",\n"
                                + "  \"pickupAddress\": {\n"
                                + "    \"detail\": \"123 Nguyễn Văn Linh, Đà Nẵng\",\n"
                                + "    \"name\": \"Nguyễn Văn Tâm\",\n"
                                + "    \"phone\": \"0905123456\",\n"
                                + "    \"latitude\": 16.0471,\n"
                                + "    \"longitude\": 108.2068\n"
                                + "  },\n"
                                + "  \"packages\": [\n"
                                + "    {\n"
                                + "      \"id\": \"4a29ec73-211a-4385-a8f3-9747bf225f18\",\n"
                                + "      \"weightKg\": 2.500,\n"
                                + "      \"packageSize\": \"M\",\n"
                                + "      \"deliveryFee\": 36.00,\n"
                                + "      \"codFee\": 150000.00,\n"
                                + "      \"payerType\": \"SENDER\",\n"
                                + "      \"category\": \"THOI_TRANG\",\n"
                                + "      \"description\": \"Áo thun nam\",\n"
                                + "      \"dropoffAddress\": {\n"
                                + "        \"detail\": \"45 Lê Duẩn, Đà Nẵng\",\n"
                                + "        \"name\": \"Trần Thị A\",\n"
                                + "        \"phone\": \"0987654321\",\n"
                                + "        \"latitude\": 16.0702,\n"
                                + "        \"longitude\": 108.2203\n"
                                + "      }\n"
                                + "    },\n"
                                + "    {\n"
                                + "      \"id\": \"e00d5a1c-3df6-46a1-ab52-26b950bf1124\",\n"
                                + "      \"weightKg\": 4.000,\n"
                                + "      \"packageSize\": \"L\",\n"
                                + "      \"deliveryFee\": 36.00,\n"
                                + "      \"codFee\": 0.00,\n"
                                + "      \"payerType\": \"RECEIVER\",\n"
                                + "      \"category\": \"QUA_TANG\",\n"
                                + "      \"description\": \"Hộp quà Tết\",\n"
                                + "      \"dropoffAddress\": {\n"
                                + "        \"detail\": \"12 Nguyễn Tri Phương, Đà Nẵng\",\n"
                                + "        \"name\": \"Phạm Văn B\",\n"
                                + "        \"phone\": \"0934567890\",\n"
                                + "        \"latitude\": 16.0653,\n"
                                + "        \"longitude\": 108.2135\n"
                                + "      }\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}"))),
    @ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<?> getOrderById(@PathVariable UUID id, Authentication auth) {
    UUID currentUserId = parsePrincipalId(auth);

    // collect roles of authenticated user
    var roles = extractRoles(auth);

    var resp = orderService.getOrderById(currentUserId, roles, id);
    return ResponseEntity.ok(resp);
  }

  private Sort buildSortFromParam(String sortParam) {
    // Accept patterns:
    // "createdAt,desc"
    // "createdAt,desc;totalAmount,asc"
    // "createdAt" (default DESC)
    if (sortParam == null || sortParam.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    try {
      // support multiple sort clauses separated by ';'
      String[] clauses = sortParam.split(";");
      List<Sort.Order> orders = new ArrayList<>();
      for (String clause : clauses) {
        String trimmed = clause.trim();
        if (trimmed.isEmpty()) continue;
        String[] parts = trimmed.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = Sort.Direction.DESC; // default
        if (parts.length > 1) {
          try {
            dir = Sort.Direction.fromString(parts[1].trim());
          } catch (IllegalArgumentException ignored) {
            // keep default
          }
        }
        orders.add(new Sort.Order(dir, property));
      }
      if (orders.isEmpty()) {
        return Sort.by(Sort.Direction.DESC, "createdAt");
      }
      return Sort.by(orders);
    } catch (Exception ex) {
      // fallback default sort
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }
  }

  // ---------------- PATCH /orders/{id}/status ----------------
  @PatchMapping("/{id}/status")
  @Operation(
      summary = "Update order status",
      description = "Update order status. Optional fields: reasonNote, failureType, newShipperId.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status updated"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "403", description = "Access denied"),
    @ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<OrderDetailResponse> updateOrderStatus(
      @PathVariable UUID id, @RequestBody OrderStatusUpdateRequest req, Authentication auth) {
    UUID currentUserId = parsePrincipalId(auth);

    Set<String> roles = extractRoles(auth);

    OrderDetailResponse resp = orderService.updateOrderStatus(currentUserId, roles, id, req);
    return ResponseEntity.ok(resp);
  }

  private UUID parsePrincipalId(Authentication auth) {
    if (auth == null || auth.getName() == null) return null;
    try {
      return UUID.fromString(auth.getName());
    } catch (Exception ex) {
      throw AppException.badRequest("Invalid principal id");
    }
  }

  private Set<String> extractRoles(Authentication auth) {
    if (auth == null) return Set.of();
    return auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  @GetMapping("/{id}/history")
  @Operation(
      summary = "Get order status history",
      description = "Get list of status changes for an order by orderId.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Order status history",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            "[\n"
                                + "  {\n"
                                + "    \"id\": \"d0ed8b41-9cde-4827-afff-a6cfb2695b26\",\n"
                                + "    \"fromStatus\": \"FINDING_DRIVER\",\n"
                                + "    \"toStatus\": \"DRIVER_ASSIGNED\",\n"
                                + "    \"changedBy\": \"c6800985-28d8-424a-899c-f714c7dcbd7f\",\n"
                                + "    \"reason\": \"Shipper đã tiếp nhận đơn\",\n"
                                + "    \"oldShipperId\": \"867c8938-37e3-4fa8-9805-2bccb70104f7\",\n"
                                + "    \"newShipperId\": \"867c8938-37e3-4fa8-9805-2bccb70104f7\",\n"
                                + "    \"createdAt\": \"2025-11-22T23:20:58.488215\"\n"
                                + "  },\n"
                                + "  {\n"
                                + "    \"id\": \"cbd86295-3a93-4b38-9c6a-06ee9f8479e8\",\n"
                                + "    \"fromStatus\": \"DRIVER_ASSIGNED\",\n"
                                + "    \"toStatus\": \"DELIVERED\",\n"
                                + "    \"changedBy\": \"c6800985-28d8-424a-899c-f714c7dcbd7f\",\n"
                                + "    \"reason\": null,\n"
                                + "    \"oldShipperId\": \"867c8938-37e3-4fa8-9805-2bccb70104f7\",\n"
                                + "    \"newShipperId\": \"867c8938-37e3-4fa8-9805-2bccb70104f7\",\n"
                                + "    \"createdAt\": \"2025-11-22T23:38:59.905308\"\n"
                                + "  }\n"
                                + "]"))),
    @ApiResponse(responseCode = "403", description = "Access denied"),
    @ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderHistory(
      @PathVariable("id") UUID orderId, Authentication auth) {

    UUID currentUserId = UUID.fromString(auth.getName());
    Set<String> roles = extractRoles(auth);

    List<OrderStatusHistoryResponse> history =
        orderService.getOrderHistory(currentUserId, roles, orderId);
    return ResponseEntity.ok(history);
  }

  @PostMapping("/price-route")
  public ResponseEntity<List<PriceAndRouteDto>> computePriceAndRoute(
      @RequestBody CreateOrderRequest request) {
    List<PriceAndRouteDto> resp = orderService.computePriceAndRouteForOrder(request);
    if (resp.isEmpty()) return ResponseEntity.status(503).build();
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/assign-shipper")
  @PreAuthorize("hasAuthority('DRIVER')")
  public ResponseEntity<OrderDetailResponse> assignShipperToOrder(
      @RequestBody AssignDriverRequest request, Authentication auth) {
    UUID driverId = UUID.fromString(auth.getName());
    OrderDetailResponse resp = orderService.assignDriverToOrder(driverId, request.orderId());
    return ResponseEntity.ok(resp);
  }

  @PatchMapping("/{id}/package-status")
  @PreAuthorize("hasAuthority('DRIVER')")
  public ResponseEntity<?> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody PackageStatusUpdateRequest request,
      Authentication auth) {
    UUID driverId = UUID.fromString(auth.getName());
    OrderDetailResponse.PackageItemResponse updated =
        orderService.updatePackageStatus(
            id,
            request.status(), // dùng record accessor
            request.note(),
            driverId);
    return ResponseEntity.ok(updated);
  }

  @PostMapping("/internal/payment-success")
  @PreAuthorize("hasAuthority('ADMIN')") // internal service auth
  public ResponseEntity<?> handlePaymentSuccess(@RequestBody PaymentSuccessRequest request) {
    orderService.findShipperAndNotifyWithPayment(request);
    return ResponseEntity.ok().build();
  }
}
