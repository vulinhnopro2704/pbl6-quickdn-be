package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FinanceOrderMetricsDto(
	@Schema(description = "Tổng số đơn hàng (all time)", example = "1205") long totalOrders,
	@Schema(description = "Số đơn đang chờ xử lý", example = "15") long pendingCount,
	@Schema(description = "Nhãn hiển thị subtitle", example = "+15 đơn chờ xử lý") String displayLabel) {}
