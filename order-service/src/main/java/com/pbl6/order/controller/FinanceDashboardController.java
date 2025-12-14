package com.pbl6.order.controller;

import com.pbl6.order.dto.FinanceDashboardResponse;
import com.pbl6.order.service.FinanceDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Finance dashboard metrics")
public class FinanceDashboardController {

  private final FinanceDashboardService financeDashboardService;

  @GetMapping("/finance-metrics")
  @Operation(
      summary = "Finance metrics",
      description = "Finance metrics for orders and revenue",
      responses =
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Finance metrics",
              content = @Content(schema = @Schema(implementation = FinanceDashboardResponse.class))))
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<FinanceDashboardResponse> getFinanceMetrics() {
    return ResponseEntity.ok(financeDashboardService.getFinanceMetrics());
  }
}
