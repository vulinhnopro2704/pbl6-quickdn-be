package com.pbl6.order.service;

import com.pbl6.order.dto.FinanceDashboardResponse;
import com.pbl6.order.dto.FinanceOrderMetricsDto;
import com.pbl6.order.dto.FinanceRevenueMetricsDto;
import com.pbl6.order.entity.OrderStatus;
import com.pbl6.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FinanceDashboardService {

  private final OrderRepository orderRepository;

  private static final List<OrderStatus> PENDING_ORDER_STATUSES = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);
  private static final List<OrderStatus> COMPLETED_STATUSES = List.of(OrderStatus.DELIVERED, OrderStatus.RETURNED);

  @Transactional(readOnly = true)
  public FinanceDashboardResponse getFinanceMetrics() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    LocalDate firstDayThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate firstDayNextMonth = firstDayThisMonth.plusMonths(1);
    LocalDate firstDayPrevMonth = firstDayThisMonth.minusMonths(1);

    LocalDateTime startThisMonth = firstDayThisMonth.atStartOfDay();
    LocalDateTime startNextMonth = firstDayNextMonth.atStartOfDay();
    LocalDateTime startPrevMonth = firstDayPrevMonth.atStartOfDay();

    // Run all queries in parallel using common ForkJoinPool
    CompletableFuture<Long> totalOrdersF = CompletableFuture.supplyAsync(orderRepository::countTotalOrders);
    CompletableFuture<Long> pendingOrdersF =
        CompletableFuture.supplyAsync(
            () -> orderRepository.countPendingOrders(PENDING_ORDER_STATUSES));
    CompletableFuture<BigDecimal> totalRevenueF =
        CompletableFuture.supplyAsync(() -> orderRepository.sumTotalRevenue(COMPLETED_STATUSES));
    CompletableFuture<BigDecimal> currentMonthRevenueF =
        CompletableFuture.supplyAsync(
            () -> orderRepository.sumRevenueBetween(COMPLETED_STATUSES, startThisMonth, startNextMonth));
    CompletableFuture<BigDecimal> lastMonthRevenueF =
        CompletableFuture.supplyAsync(
            () -> orderRepository.sumRevenueBetween(COMPLETED_STATUSES, startPrevMonth, startThisMonth));

    long totalOrders = totalOrdersF.join();
    long pendingOrders = pendingOrdersF.join();
    BigDecimal totalRevenue = totalRevenueF.join();
    BigDecimal currentMonthRevenue = currentMonthRevenueF.join();
    BigDecimal lastMonthRevenue = lastMonthRevenueF.join();

    double growthPercent = computeGrowth(currentMonthRevenue, lastMonthRevenue);

    FinanceOrderMetricsDto orderMetrics =
        new FinanceOrderMetricsDto(totalOrders, pendingOrders, "+" + pendingOrders + " đơn chờ xử lý");
    FinanceRevenueMetricsDto revenueMetrics =
        new FinanceRevenueMetricsDto(
            totalRevenue,
            growthPercent,
            formatGrowthLabel(growthPercent));

    return new FinanceDashboardResponse(orderMetrics, revenueMetrics);
  }

  private static double computeGrowth(BigDecimal current, BigDecimal previous) {
    if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
      return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }
    if (current == null) current = BigDecimal.ZERO;
    BigDecimal diff = current.subtract(previous);
    return diff
        .multiply(BigDecimal.valueOf(100))
        .divide(previous, 2, RoundingMode.HALF_UP)
        .doubleValue();
  }

  private static String formatGrowthLabel(double growthPercent) {
    String sign = growthPercent >= 0 ? "+" : "";
    return sign + growthPercent + "% so với tháng trước";
  }
}
