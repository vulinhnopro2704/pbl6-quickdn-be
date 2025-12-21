package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record MonthlyRevenueItem(
        @Schema(description = "Năm", example = "2024") int year,
        @Schema(description = "Tháng (1-12)", example = "5") int month,
        @Schema(description = "Tổng doanh thu (đơn hoàn thành)", example = "12500000") BigDecimal revenue,
        @Schema(description = "Số đơn hoàn thành", example = "123") long orderCount) {
}
