package com.pbl6.order.controller;

import com.pbl6.order.dto.HeatmapRequest;
import com.pbl6.order.dto.HeatmapResponse;
import com.pbl6.order.dto.StatusCountsRequest;
import com.pbl6.order.dto.StatusCountsResponse;
import com.pbl6.order.service.RevenueHeatmapService;
import com.pbl6.order.service.StatusStatisticsService;
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
  private final StatusStatisticsService statusStatisticsService;

  @PostMapping("/heatmap/revenue")
  @Operation(summary = "Revenue heatmap", description = "Aggregate revenue for heatmap by district/ward and pickup/delivery view")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<HeatmapResponse> getRevenueHeatmap(@Valid @RequestBody HeatmapRequest req) {
    return ResponseEntity.ok(revenueHeatmapService.getRevenueHeatmap(req));
  }

  @PostMapping("/status-counts")
  @Operation(
      summary = "Status counts",
      description = "Thống kê số lượng đơn/gói theo trạng thái, lọc theo thời gian và quận")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StatusCountsResponse> getStatusCounts(
      @Valid @RequestBody StatusCountsRequest req) {
    return ResponseEntity.ok(statusStatisticsService.getStatusCounts(req));
  }
}
