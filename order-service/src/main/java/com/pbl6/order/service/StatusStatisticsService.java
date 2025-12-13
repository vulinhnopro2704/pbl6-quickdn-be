package com.pbl6.order.service;

import com.pbl6.order.dto.StatusCountItem;
import com.pbl6.order.dto.StatusCountsRequest;
import com.pbl6.order.dto.StatusCountsResponse;
import com.pbl6.order.entity.OrderStatus;
import com.pbl6.order.entity.PackageStatus;
import com.pbl6.order.repository.OrderRepository;
import com.pbl6.order.repository.PackageRepository;
import com.pbl6.order.repository.projection.OrderStatusCountProjection;
import com.pbl6.order.repository.projection.PackageStatusCountProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusStatisticsService {

  private final OrderRepository orderRepository;
  private final PackageRepository packageRepository;

  private static final Map<OrderStatus, StatusMeta> ORDER_META =
      Map.ofEntries(
          Map.entry(OrderStatus.PENDING, new StatusMeta(1, "Chờ xử lý", "#6c757d")),
          Map.entry(OrderStatus.CONFIRMED, new StatusMeta(2, "Đã xác nhận", "#0d6efd")),
          Map.entry(OrderStatus.PICKED, new StatusMeta(3, "Đã lấy", "#0d6efd")),
          Map.entry(OrderStatus.CANCELLED, new StatusMeta(4, "Đã hủy (legacy)", "#dc3545")),
          Map.entry(OrderStatus.FINDING_DRIVER, new StatusMeta(10, "Đang tìm tài xế", "#0dcaf0")),
          Map.entry(OrderStatus.DRIVER_ASSIGNED, new StatusMeta(11, "Đã gán tài xế", "#0d6efd")),
          Map.entry(
              OrderStatus.DRIVER_EN_ROUTE_PICKUP,
              new StatusMeta(12, "Tài xế đang đến điểm lấy", "#fd7e14")),
          Map.entry(OrderStatus.ARRIVED_PICKUP, new StatusMeta(13, "Đã tới điểm lấy", "#fd7e14")),
          Map.entry(OrderStatus.PICKUP_ATTEMPT_FAILED, new StatusMeta(14, "Không liên lạc người gửi", "#dc3545")),
          Map.entry(OrderStatus.PICKUP_FAILED, new StatusMeta(15, "Lấy hàng thất bại", "#dc3545")),
          Map.entry(OrderStatus.PACKAGE_PICKED, new StatusMeta(16, "Đã lấy hàng", "#0d6efd")),
          Map.entry(OrderStatus.EN_ROUTE_DELIVERY, new StatusMeta(20, "Đang giao hàng", "#fd7e14")),
          Map.entry(OrderStatus.ARRIVED_DELIVERY, new StatusMeta(21, "Đã tới điểm giao", "#fd7e14")),
          Map.entry(
              OrderStatus.DELIVERY_ATTEMPT_FAILED,
              new StatusMeta(22, "Không liên lạc người nhận", "#dc3545")),
          Map.entry(OrderStatus.DELIVERY_FAILED, new StatusMeta(23, "Giao thất bại", "#dc3545")),
          Map.entry(OrderStatus.DELIVERED, new StatusMeta(24, "Giao thành công", "#198754")),
          Map.entry(OrderStatus.RETURNING_TO_SENDER, new StatusMeta(30, "Đang hoàn trả", "#ffc107")),
          Map.entry(OrderStatus.RETURNED, new StatusMeta(31, "Đã hoàn trả", "#198754")),
          Map.entry(OrderStatus.DRIVER_ISSUE_REPORTED, new StatusMeta(40, "Tài xế báo sự cố", "#6f42c1")),
          Map.entry(OrderStatus.REASSIGNING_DRIVER, new StatusMeta(41, "Đang đổi tài xế", "#6f42c1")),
          Map.entry(OrderStatus.CANCELLED_BY_SENDER, new StatusMeta(50, "Người gửi hủy", "#dc3545")),
          Map.entry(OrderStatus.CANCELLED_BY_DRIVER, new StatusMeta(51, "Tài xế hủy", "#dc3545")),
          Map.entry(OrderStatus.CANCELLED_NO_DRIVER, new StatusMeta(52, "Không tìm được tài xế", "#dc3545")),
          Map.entry(OrderStatus.ORDER_CANCELLED, new StatusMeta(53, "Đơn hàng bị hủy", "#dc3545")));

  private static final Map<PackageStatus, StatusMeta> PACKAGE_META =
      Map.ofEntries(
          Map.entry(PackageStatus.WAITING_FOR_PICKUP, new StatusMeta(101, "Chờ lấy hàng", "#6c757d")),
          Map.entry(PackageStatus.PICKED_UP, new StatusMeta(102, "Đã lấy hàng", "#0d6efd")),
          Map.entry(PackageStatus.PICKUP_ATTEMPT_FAILED, new StatusMeta(103, "Không liên lạc người gửi", "#dc3545")),
          Map.entry(PackageStatus.PICKUP_FAILED, new StatusMeta(104, "Lấy thất bại", "#dc3545")),
          Map.entry(PackageStatus.WAITING_FOR_DELIVERY, new StatusMeta(105, "Chờ giao hàng", "#6c757d")),
          Map.entry(PackageStatus.DELIVERY_IN_PROGRESS, new StatusMeta(106, "Đang giao", "#fd7e14")),
          Map.entry(PackageStatus.DELIVERY_ATTEMPT_FAILED, new StatusMeta(107, "Không liên lạc người nhận", "#dc3545")),
          Map.entry(PackageStatus.DELIVERY_FAILED, new StatusMeta(108, "Giao thất bại", "#dc3545")),
          Map.entry(PackageStatus.DELIVERED, new StatusMeta(109, "Giao thành công", "#198754")),
          Map.entry(PackageStatus.RETURNING, new StatusMeta(110, "Đang hoàn trả", "#ffc107")),
          Map.entry(PackageStatus.RETURNED, new StatusMeta(111, "Đã hoàn trả", "#198754")),
          Map.entry(PackageStatus.CANCELLED, new StatusMeta(112, "Đã hủy", "#dc3545")));

  @Transactional(readOnly = true)
  public StatusCountsResponse getStatusCounts(StatusCountsRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    LocalDateTime from = request.fromDate();
    LocalDateTime to = request.toDate();

    if (from != null && to != null && from.isAfter(to)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "fromDate must be before or equal to toDate");
    }

    log.info(
        "StatusCounts request target={}, districtCode={}, from={}, to={}",
        request.target(),
        request.districtCode(),
        from,
        to);

    List<StatusCountItem> data =
        switch (request.target()) {
          case ORDER -> aggregateOrders(from, to, request.districtCode());
          case PACKAGE -> aggregatePackages(from, to, request.districtCode());
        };

    return new StatusCountsResponse(request.target(), data);
  }

  private List<StatusCountItem> aggregateOrders(LocalDateTime from, LocalDateTime to, Integer districtCode) {
    log.info("StatusCounts ORDER query from={}, to={}, districtCode={}", from, to, districtCode);

    Map<OrderStatus, Long> counts =
        orderRepository.aggregateStatusCounts(from, to, districtCode).stream()
            .filter(p -> p.getStatus() != null)
            .collect(Collectors.toMap(OrderStatusCountProjection::getStatus, OrderStatusCountProjection::getCount));

    log.info("StatusCounts ORDER result size={}, counts={}", counts.size(), counts);

    return buildItems(OrderStatus.values(), counts, ORDER_META);
  }

  private List<StatusCountItem> aggregatePackages(LocalDateTime from, LocalDateTime to, Integer districtCode) {
    log.info("StatusCounts PACKAGE query from={}, to={}, districtCode={}", from, to, districtCode);

    Map<PackageStatus, Long> counts =
        packageRepository.aggregateStatusCounts(from, to, districtCode).stream()
            .filter(p -> p.getStatus() != null)
            .collect(Collectors.toMap(PackageStatusCountProjection::getStatus, PackageStatusCountProjection::getCount));

    log.info("StatusCounts PACKAGE result size={}, counts={}", counts.size(), counts);

    return buildItems(PackageStatus.values(), counts, PACKAGE_META);
  }

  private <E extends Enum<E>> List<StatusCountItem> buildItems(
      E[] allStatuses, Map<E, Long> counts, Map<E, StatusMeta> metaMap) {
    List<StatusCountItem> items = new ArrayList<>();
    for (E status : allStatuses) {
      StatusMeta meta = Optional.ofNullable(metaMap.get(status)).orElse(defaultMeta(status));
      long count = counts.getOrDefault(status, 0L);
      if (count > 0) {
        items.add(new StatusCountItem(meta.code(), status.name(), meta.label(), meta.colorCode(), count));
      }
    }

    items.sort(Comparator.comparingInt(StatusCountItem::statusCode));
    return items;
  }

  private static StatusMeta defaultMeta(Enum<?> status) {
    return new StatusMeta(status.ordinal(), status.name(), "#6c757d");
  }

  private record StatusMeta(int code, String label, String colorCode) {}
}
