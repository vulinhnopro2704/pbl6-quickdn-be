package com.pbl6.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record FinanceRevenueMetricsDto(
	@Schema(description = "Tổng doanh thu từ đơn hoàn thành", example = "250000000")
	BigDecimal totalRevenue,
	@Schema(description = "Tăng trưởng % so với tháng trước", example = "12.5")
	Double growthPercentage,
	@Schema(description = "Nhãn hiển thị subtitle", example = "+12.5% so với tháng trước")
	String displayLabel) {}
