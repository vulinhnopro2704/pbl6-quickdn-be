package com.pbl6.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record HeatmapRequest(
  @Schema(description = "Từ ngày (local time)", example = "2024-01-01T00:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime fromDate,

  @Schema(description = "Đến ngày (local time)", example = "2024-01-31T23:59:59")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime toDate,

  GroupByType groupBy,
  ViewType viewType) {
  public enum GroupByType {
    DISTRICT,
    WARD
  }

  public enum ViewType {
    PICKUP,
    DELIVERY
  }
}
