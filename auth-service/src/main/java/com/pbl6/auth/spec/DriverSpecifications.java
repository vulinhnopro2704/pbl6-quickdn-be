package com.pbl6.auth.spec;

import com.pbl6.auth.entity.DriverEntity;
import com.pbl6.auth.entity.DriverStatus;
import com.pbl6.auth.entity.Gender;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DriverSpecifications {

  public static Specification<DriverEntity> search(String q) {
    if (q == null || q.isBlank()) return null;
    String like = "%" + q.toLowerCase().trim() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("identityFullName")), like),
            cb.like(cb.lower(root.get("identityNumber")), like),
            cb.like(cb.lower(root.get("vehiclePlateNumber")), like),
            cb.like(cb.lower(root.get("licenseNumber")), like));
  }

  public static Specification<DriverEntity> hasStatus(DriverStatus status) {
    return status == null ? null : (root, q, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<DriverEntity> hasGender(Gender gender) {
    return gender == null ? null : (root, q, cb) -> cb.equal(root.get("identityGender"), gender);
  }

  public static Specification<DriverEntity> createdFrom(LocalDateTime from) {
    return from == null
        ? null
        : (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<DriverEntity> createdTo(LocalDateTime to) {
    return to == null ? null : (root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<DriverEntity> ratingMin(Integer value) {
    return value == null
        ? null
        : (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("ratingAvg"), value);
  }

  public static Specification<DriverEntity> ratingMax(Integer value) {
    return value == null
        ? null
        : (root, q, cb) -> cb.lessThanOrEqualTo(root.get("ratingAvg"), value);
  }
}
