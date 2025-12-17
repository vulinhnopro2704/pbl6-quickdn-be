package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload cho widget đơn hàng và doanh thu")
public record FinanceDashboardResponse(
    @Schema(description = "Thông tin widget đơn hàng") FinanceOrderMetricsDto orderMetrics,
    @Schema(description = "Thông tin widget doanh thu") FinanceRevenueMetricsDto revenueMetrics) {}
