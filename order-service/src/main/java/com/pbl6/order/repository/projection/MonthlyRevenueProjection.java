package com.pbl6.order.repository.projection;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    Integer getYear();

    Integer getMonth();

    BigDecimal getRevenue();

    Long getOrderCount();
}
