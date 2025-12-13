package com.pbl6.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record StatusCountsRequest(
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant fromDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant toDate,
    @NotNull Target target,
    Integer districtCode) {

  public enum Target {
    ORDER,
    PACKAGE
  }
}
