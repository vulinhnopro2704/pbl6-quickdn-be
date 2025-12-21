package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Kết quả thống kê doanh thu theo tháng")
public record MonthlyRevenueResponse(
        @Schema(description = "Danh sách doanh thu theo tháng") List<MonthlyRevenueItem> data) {
}
