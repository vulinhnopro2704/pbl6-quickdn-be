package com.pbl6.order.dto;

import java.math.BigDecimal;

public record HeatmapPointDto(
    Integer districtCode,
    String districtName,
    Integer wardCode,
    String wardName,
    BigDecimal totalRevenue,
    Long orderCount,
    CoordinateDto coordinates) {}
