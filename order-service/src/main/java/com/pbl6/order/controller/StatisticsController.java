package com.pbl6.order.controller;

import com.pbl6.order.dto.HeatmapRequest;
import com.pbl6.order.dto.HeatmapResponse;
import com.pbl6.order.dto.StatusCountsRequest;
import com.pbl6.order.dto.StatusCountsResponse;
import com.pbl6.order.service.RevenueHeatmapService;
import com.pbl6.order.service.StatusStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Revenue heatmap statistics")
public class StatisticsController {

  private final RevenueHeatmapService revenueHeatmapService;
  private final StatusStatisticsService statusStatisticsService;

  @PostMapping("/heatmap/revenue")
  @Operation(summary = "Revenue heatmap", description = "Aggregate revenue for heatmap by district/ward and pickup/delivery view")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Heatmap filter",
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = HeatmapRequest.class),
              examples =
                  @ExampleObject(
                      name = "Heatmap request",
                      value =
                          "{\n"
                              + "  \"fromDate\": \"2024-01-01T00:00:00\",\n"
                              + "  \"toDate\": \"2024-01-31T23:59:59\",\n"
                              + "  \"groupBy\": \"DISTRICT\",\n"
                              + "  \"viewType\": \"PICKUP\"\n"
                              + "}")))
  @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HeatmapResponse> getRevenueHeatmap(
      @Valid @RequestBody HeatmapRequest req) {
    HeatmapRequest resolved = applyDefaults(req);
    validateHeatmapRequest(resolved);
    return ResponseEntity.ok(revenueHeatmapService.getRevenueHeatmap(resolved));
  }

  @PostMapping("/status-counts")
  @Operation(
      summary = "Status counts",
      description = "Thống kê số lượng đơn/gói theo trạng thái, lọc theo thời gian và quận")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Status count filter",
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = StatusCountsRequest.class),
              examples =
                  @ExampleObject(
                      name = "Status counts request",
                      value =
                          "{\n"
                              + "  \"fromDate\": \"2024-01-01T00:00:00\",\n"
                              + "  \"toDate\": \"2024-01-31T23:59:59\",\n"
                              + "  \"target\": \"ORDER\",\n"
                              + "  \"districtCode\": 490\n"
                              + "}")))
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<StatusCountsResponse> getStatusCounts(
      @Valid @RequestBody StatusCountsRequest req) {
    StatusCountsRequest resolved = applyDefaults(req);
    validateStatusCountsRequest(resolved);
    return ResponseEntity.ok(statusStatisticsService.getStatusCounts(resolved));
  }

  private void validateHeatmapRequest(HeatmapRequest req) {
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
    }

    if (req.fromDate() != null && req.toDate() != null && req.fromDate().isAfter(req.toDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be before or equal to toDate");
    }

    if (req.groupBy() == null || req.viewType() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "groupBy and viewType are required");
    }
  }

  private void validateStatusCountsRequest(StatusCountsRequest req) {
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
    }

    if (req.fromDate() != null && req.toDate() != null && req.fromDate().isAfter(req.toDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be before or equal to toDate");
    }

    if (req.target() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "target is required");
    }
  }

  private HeatmapRequest applyDefaults(HeatmapRequest req) {
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
    }

    HeatmapRequest.GroupByType groupBy = req.groupBy() != null ? req.groupBy() : HeatmapRequest.GroupByType.DISTRICT;
    HeatmapRequest.ViewType viewType = req.viewType() != null ? req.viewType() : HeatmapRequest.ViewType.PICKUP;

    return new HeatmapRequest(req.fromDate(), req.toDate(), groupBy, viewType);
  }

  private StatusCountsRequest applyDefaults(StatusCountsRequest req) {
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
    }

    StatusCountsRequest.Target target = req.target() != null ? req.target() : StatusCountsRequest.Target.ORDER;
    return new StatusCountsRequest(req.fromDate(), req.toDate(), target, req.districtCode());
  }
}
