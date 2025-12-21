package com.pbl6.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Yêu cầu thống kê doanh thu theo tháng")
public record MonthlyRevenueRequest(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") @Schema(description = "Từ ngày (local time, tính từ đầu tháng chứa fromDate)", example = "2024-01-01T00:00:00") LocalDateTime fromDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") @Schema(description = "Đến ngày (local time, tính tới hết tháng chứa toDate)", example = "2024-12-31T23:59:59") LocalDateTime toDate) {
}
