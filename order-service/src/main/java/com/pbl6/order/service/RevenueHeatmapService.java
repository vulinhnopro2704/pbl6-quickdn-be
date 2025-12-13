package com.pbl6.order.service;

import com.pbl6.order.constant.DistrictEnum;
import com.pbl6.order.constant.WardEnum;
import com.pbl6.order.dto.CoordinateDto;
import com.pbl6.order.dto.HeatmapPointDto;
import com.pbl6.order.dto.HeatmapRequest;
import com.pbl6.order.dto.HeatmapResponse;
import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.entity.PackageAddressEntity;
import com.pbl6.order.entity.PackageEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RevenueHeatmapService {

  @PersistenceContext private EntityManager entityManager;
  private final LocationCoordinateService coordinateService;

  @Transactional(readOnly = true)
  public HeatmapResponse getRevenueHeatmap(HeatmapRequest req) {
    if (req == null) return new HeatmapResponse(List.of());

    LocalDateTime from = req.fromDate();
    LocalDateTime to = req.toDate();

    List<HeatmapPointDto> points =
        switch (req.viewType()) {
          case PICKUP -> queryPickup(req.groupBy(), from, to);
          case DELIVERY -> queryDelivery(req.groupBy(), from, to);
        };

    return new HeatmapResponse(points);
  }

  private List<HeatmapPointDto> queryPickup(HeatmapRequest.GroupByType groupBy, LocalDateTime from, LocalDateTime to) {
    var cb = entityManager.getCriteriaBuilder();
    var cq = cb.createTupleQuery();

    Root<OrderEntity> order = cq.from(OrderEntity.class);
    Join<OrderEntity, PackageAddressEntity> pickup = order.join("pickupAddress", JoinType.LEFT);

    Expression<Integer> district = pickup.get("districtCode");
    Expression<Integer> ward = pickup.get("wardCode");

    Expression<BigDecimal> totalAmount = order.get("totalAmount");
    Expression<Long> countExpr = cb.count(order);
    Expression<BigDecimal> revenueExpr = cb.sum(totalAmount);

    Predicate predicate = cb.conjunction();
    if (from != null) {
      predicate = cb.and(predicate, cb.greaterThanOrEqualTo(order.get("createdAt"), from));
    }
    if (to != null) {
      predicate = cb.and(predicate, cb.lessThanOrEqualTo(order.get("createdAt"), to));
    }

    if (groupBy == HeatmapRequest.GroupByType.WARD) {
      cq.multiselect(district.alias("district"), ward.alias("ward"), revenueExpr.alias("revenue"), countExpr.alias("count"))
          .where(predicate)
          .groupBy(district, ward);
    } else {
      cq.multiselect(district.alias("district"), revenueExpr.alias("revenue"), countExpr.alias("count"))
          .where(predicate)
          .groupBy(district);
    }

    List<Tuple> tuples = entityManager.createQuery(cq).getResultList();
    return tuplesToPoints(tuples, groupBy);
  }

  private List<HeatmapPointDto> queryDelivery(HeatmapRequest.GroupByType groupBy, LocalDateTime from, LocalDateTime to) {
    var cb = entityManager.getCriteriaBuilder();
    var cq = cb.createTupleQuery();

    Root<PackageEntity> pkg = cq.from(PackageEntity.class);
    Join<PackageEntity, PackageAddressEntity> drop = pkg.join("dropoffAddress", JoinType.LEFT);
    Join<PackageEntity, OrderEntity> order = pkg.join("order", JoinType.INNER);

    Expression<Integer> district = drop.get("districtCode");
    Expression<Integer> ward = drop.get("wardCode");

    Expression<BigDecimal> deliveryFee = pkg.get("deliveryFee");
    Expression<Long> countExpr = cb.count(pkg);
    Expression<BigDecimal> revenueExpr = cb.sum(deliveryFee);

    Predicate predicate = cb.conjunction();
    if (from != null) {
      predicate = cb.and(predicate, cb.greaterThanOrEqualTo(order.get("createdAt"), from));
    }
    if (to != null) {
      predicate = cb.and(predicate, cb.lessThanOrEqualTo(order.get("createdAt"), to));
    }

    if (groupBy == HeatmapRequest.GroupByType.WARD) {
      cq.multiselect(district.alias("district"), ward.alias("ward"), revenueExpr.alias("revenue"), countExpr.alias("count"))
          .where(predicate)
          .groupBy(district, ward);
    } else {
      cq.multiselect(district.alias("district"), revenueExpr.alias("revenue"), countExpr.alias("count"))
          .where(predicate)
          .groupBy(district);
    }

    List<Tuple> tuples = entityManager.createQuery(cq).getResultList();
    return tuplesToPoints(tuples, groupBy);
  }

  private List<HeatmapPointDto> tuplesToPoints(List<Tuple> tuples, HeatmapRequest.GroupByType groupBy) {
    List<HeatmapPointDto> points = new ArrayList<>();
    for (Tuple t : tuples) {
      Integer districtCode = t.get("district", Integer.class);
      Integer wardCode = groupBy == HeatmapRequest.GroupByType.WARD ? t.get("ward", Integer.class) : null;
      BigDecimal revenue = Optional.ofNullable(t.get("revenue", BigDecimal.class)).orElse(BigDecimal.ZERO);
      Long count = Optional.ofNullable(t.get("count", Long.class)).orElse(0L);

      // Infer district from ward if missing
      if (districtCode == null && wardCode != null) {
        districtCode = WardEnum.fromCode(wardCode).map(WardEnum::getDistrict).map(DistrictEnum::getCode).orElse(null);
      }

      String districtName = DistrictEnum.fromCode(districtCode).map(DistrictEnum::getFullName).orElse(null);
      String wardName = WardEnum.fromCode(wardCode).map(WardEnum::getFullName).orElse(null);

      CoordinateDto coordinate = resolveCoordinate(groupBy, districtCode, wardCode);

      points.add(new HeatmapPointDto(districtCode, districtName, wardCode, wardName, revenue, count, coordinate));
    }
    return points;
  }

  private CoordinateDto resolveCoordinate(HeatmapRequest.GroupByType groupBy, Integer districtCode, Integer wardCode) {
    return switch (groupBy) {
      case DISTRICT ->
          coordinateService
              .findDistrictCoordinate(districtCode)
              .map(c -> new CoordinateDto(c.lat(), c.lng()))
              .orElse(null);
      case WARD ->
          coordinateService
              .findWardCoordinate(wardCode)
              .or(() -> coordinateService.findDistrictCoordinate(districtCode))
              .map(c -> new CoordinateDto(c.lat(), c.lng()))
              .orElse(null);
    };
  }
}
