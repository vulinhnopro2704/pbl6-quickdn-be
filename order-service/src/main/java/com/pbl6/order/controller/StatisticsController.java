package com.pbl6.order.controller;

import com.pbl6.order.dto.HeatmapRequest;
import com.pbl6.order.dto.HeatmapResponse;
import com.pbl6.order.service.RevenueHeatmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Revenue heatmap statistics")
public class StatisticsController {

  private final RevenueHeatmapService revenueHeatmapService;

  @PostMapping("/heatmap/revenue")
  @Operation(summary = "Revenue heatmap", description = "Aggregate revenue for heatmap by district/ward and pickup/delivery view")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HeatmapResponse> getRevenueHeatmap(@Valid @RequestBody HeatmapRequest req) {
    return ResponseEntity.ok(revenueHeatmapService.getRevenueHeatmap(req));
  }
}
