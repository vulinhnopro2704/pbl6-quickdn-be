package com.pbl6.order.spec;

import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.entity.PackageEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OrderSpecifications {

  private OrderSpecifications() {}

  public static Specification<OrderEntity> belongsToUser(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("creatorId"), userId);
  }

  public static Specification<OrderEntity> hasStatus(String status) {
    return (root, query, cb) ->
        cb.equal(root.get("status"), Enum.valueOf(com.pbl6.order.entity.OrderStatus.class, status));
  }

  public static Specification<OrderEntity> hasPaymentMethod(String method) {
    return (root, query, cb) ->
        cb.equal(
            root.get("paymentMethod"),
            Enum.valueOf(com.pbl6.order.entity.PaymentMethod.class, method));
  }

  public static Specification<OrderEntity> hasShipperId(UUID shipperId) {
    return (root, query, cb) -> cb.equal(root.get("shipperId"), shipperId);
  }

  public static Specification<OrderEntity> fromDate(LocalDate fromDate) {
    LocalDateTime from = fromDate.atStartOfDay();
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<OrderEntity> toDate(LocalDate toDate) {
    LocalDateTime to = toDate.atTime(LocalTime.MAX);
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<OrderEntity> freeTextSearch(String q) {
    String trimmed = q == null ? "" : q.trim();
    if (trimmed.isEmpty()) return (root, query, cb) -> null;

    String like = "%" + trimmed.toLowerCase() + "%";

    return (root, query, cb) -> {
      query.distinct(true);

      List<Predicate> ors = new ArrayList<>();

      // 1) nếu q là UUID hợp lệ -> so sánh equality trên id
      try {
        UUID uuid = UUID.fromString(trimmed);
        ors.add(cb.equal(root.get("id"), uuid));
      } catch (IllegalArgumentException ignored) {
        // not a UUID -> skip
      }

      // 2) search trong text fields (pickupAddress.detail, package.description,
      // dropoffAddress.detail, dropoffAddress.name, customerNote, ...)
      try {
        Expression<String> pickupDetail = cb.lower(root.get("pickupAddress").get("detail"));
        ors.add(cb.like(pickupDetail, like));
      } catch (IllegalArgumentException ignored) {
      }

      try {
        Join<OrderEntity, PackageEntity> pkg = root.join("packages", JoinType.LEFT);
        ors.add(cb.like(cb.lower(pkg.get("description")), like));
        ors.add(cb.like(cb.lower(pkg.get("dropoffAddress").get("detail")), like));
        ors.add(cb.like(cb.lower(pkg.get("dropoffAddress").get("name")), like));
      } catch (IllegalArgumentException ignored) {
      }

      // 3) search customerNote
      try {
        ors.add(cb.like(cb.lower(root.get("customerNote")), like));
      } catch (IllegalArgumentException ignored) {
      }

      // combine ORs
      if (ors.isEmpty()) {
        return cb.conjunction(); // hoặc cb.isTrue(cb.literal(true))
      }
      return cb.or(ors.toArray(new Predicate[0]));
    };
  }
}
