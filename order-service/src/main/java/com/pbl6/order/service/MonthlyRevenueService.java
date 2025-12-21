package com.pbl6.order.service;

import com.pbl6.order.dto.MonthlyRevenueItem;
import com.pbl6.order.dto.MonthlyRevenueRequest;
import com.pbl6.order.dto.MonthlyRevenueResponse;
import com.pbl6.order.entity.OrderStatus;
import com.pbl6.order.repository.OrderRepository;
import com.pbl6.order.repository.projection.MonthlyRevenueProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MonthlyRevenueService {

    private final OrderRepository orderRepository;

    private static final List<OrderStatus> COMPLETED_STATUSES = List.of(OrderStatus.DELIVERED, OrderStatus.RETURNED);

    @Transactional(readOnly = true)
    public MonthlyRevenueResponse getMonthlyRevenue(MonthlyRevenueRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        LocalDate fromMonthStart = toMonthStart(request.fromDate());
        LocalDate toMonthStart = toMonthStart(request.toDate());
        LocalDate toExclusiveMonthStart = toMonthStart.plusMonths(1);

        LocalDateTime fromDateTime = fromMonthStart.atStartOfDay();
        LocalDateTime toExclusiveDateTime = toExclusiveMonthStart.atStartOfDay();

        List<MonthlyRevenueProjection> aggregates = orderRepository.aggregateMonthlyRevenue(COMPLETED_STATUSES,
                fromDateTime, toExclusiveDateTime);

        Map<YearMonth, MonthlyRevenueProjection> aggregateByMonth = new HashMap<>();
        for (MonthlyRevenueProjection projection : aggregates) {
            if (projection.getYear() != null && projection.getMonth() != null) {
                YearMonth ym = YearMonth.of(projection.getYear(), projection.getMonth());
                aggregateByMonth.putIfAbsent(ym, projection);
            }
        }

        List<MonthlyRevenueItem> items = new ArrayList<>();
        YearMonth cursor = YearMonth.from(fromMonthStart);
        YearMonth endExclusive = YearMonth.from(toExclusiveMonthStart);

        while (cursor.isBefore(endExclusive)) {
            MonthlyRevenueProjection agg = aggregateByMonth.get(cursor);
            BigDecimal revenue = agg != null && agg.getRevenue() != null ? agg.getRevenue() : BigDecimal.ZERO;
            long orderCount = agg != null && agg.getOrderCount() != null ? agg.getOrderCount() : 0L;

            items.add(new MonthlyRevenueItem(cursor.getYear(), cursor.getMonthValue(), revenue, orderCount));
            cursor = cursor.plusMonths(1);
        }

        return new MonthlyRevenueResponse(items);
    }

    private static LocalDate toMonthStart(LocalDateTime input) {
        LocalDate base = input != null ? input.toLocalDate() : LocalDate.now(ZoneOffset.UTC);
        return base.with(TemporalAdjusters.firstDayOfMonth());
    }
}
