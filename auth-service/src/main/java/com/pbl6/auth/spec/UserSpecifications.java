package com.pbl6.auth.spec;

import com.pbl6.auth.entity.Role;
import com.pbl6.auth.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;

public final class UserSpecifications {

  private UserSpecifications() {}

  public static Specification<User> searchByPhoneOrName(String q) {
    return (root, query, cb) -> {
      if (q == null || q.isBlank()) return null;
      String pattern = "%" + q.trim().toLowerCase() + "%";
      Expression<String> phoneExpr = cb.lower(root.get("phone"));
      Expression<String> nameExpr = cb.lower(root.get("fullName"));
      return cb.or(cb.like(phoneExpr, pattern), cb.like(nameExpr, pattern));
    };
  }

  public static Specification<User> hasRole(Role role) {
    return (root, query, cb) -> {
      if (role == null) return null;
      // join to roles collection
      Join<User, Role> rolesJoin = root.join("roles", JoinType.LEFT);
      // distinct results
      query.distinct(true);
      return cb.equal(rolesJoin, role);
    };
  }

  public static Specification<User> hasEnabled(Boolean enabled) {
    return (root, query, cb) -> enabled == null ? null : cb.equal(root.get("enabled"), enabled);
  }

  public static Specification<User> hasIsActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<User> createdBetween(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
      if (from == null && to == null) return null;
      if (from != null && to != null) {
        return cb.between(root.get("createdAt"), from, to);
      } else if (from != null) {
        return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
      } else {
        return cb.lessThanOrEqualTo(root.get("createdAt"), to);
      }
    };
  }

  public static Specification<User> build(
      String q,
      Role role,
      Boolean enabled,
      Boolean isActive,
      LocalDateTime createdFrom,
      LocalDateTime createdTo) {
    return Specification.allOf(
        searchByPhoneOrName(q),
        hasRole(role),
        hasEnabled(enabled),
        hasIsActive(isActive),
        createdBetween(createdFrom, createdTo));
  }
}
