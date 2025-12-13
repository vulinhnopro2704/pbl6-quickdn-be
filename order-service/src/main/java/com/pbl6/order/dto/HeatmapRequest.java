package com.pbl6.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record HeatmapRequest(
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant fromDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant toDate,
    @NotNull GroupByType groupBy,
    @NotNull ViewType viewType,
    String cityCode) {
  public enum GroupByType {
    DISTRICT,
    WARD
  }

  public enum ViewType {
    PICKUP,
    DELIVERY
  }
}
