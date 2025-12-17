package com.pbl6.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Yêu cầu thống kê trạng thái đơn/gói")
public record StatusCountsRequest(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Từ ngày (local time)", example = "2023-01-01T00:00:00")
    LocalDateTime fromDate,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Đến ngày (local time)", example = "2023-12-31T23:59:59")
    LocalDateTime toDate,

    @Schema(description = "Chọn nguồn thống kê", allowableValues = {"ORDER", "PACKAGE"})
    Target target,

    @Schema(description = "Mã quận (lọc theo pickup nếu ORDER, dropoff nếu PACKAGE)", example = "490")
    Integer districtCode) {

  public enum Target {
    ORDER,
    PACKAGE
  }
}
