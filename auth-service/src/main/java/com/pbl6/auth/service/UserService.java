package com.pbl6.auth.service;

import com.pbl6.auth.dto.*;
import com.pbl6.auth.entity.Role;
import com.pbl6.auth.entity.User;
import com.pbl6.auth.entity.UserAddress;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.UserAddressRepository;
import com.pbl6.auth.repository.UserRepository;
import com.pbl6.auth.spec.UserSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepo;
  private final UserAddressRepository addressRepo;

  public UserResponse getById(UUID userId) {
    User user =
        userRepo.findById(userId).orElseThrow(() -> AppException.notFound("User not found"));
    return toResponse(user);
  }

  @Transactional
  public UserResponse updateById(UUID id, UserUpdateRequest req) {
    User u = userRepo.findById(id).orElseThrow(() -> AppException.notFound("User not found"));

    // Chỉ cập nhật những field cho phép qua DTO
    if (req.fullName() != null) {
      u.setFullName(req.fullName().trim());
    }
    if (req.dob() != null) {
      u.setDob(req.dob());
    }

    u = userRepo.save(u);
    return toResponse(u);
  }

  @Transactional
  public UserResponse updateSelf(UUID userId, UserUpdateRequest req) {
    return updateById(userId, req);
  }

  private UserResponse toResponse(User u) {
    Set<UserAddressResponse> addrResponses =
        u.getAddresses().stream()
            .map(
                a ->
                    new UserAddressResponse(
                        a.getId(),
                        a.getAddressLine(),
                        a.getWard(),
                        a.getDistrict(),
                        a.getProvince(),
                        a.getCountry(),
                        a.getLatitude(),
                        a.getLongitude(),
                        a.getNote(),
                        a.isPrimary(),
                        a.getCreatedAt(),
                        a.getUpdatedAt()))
            .collect(Collectors.toSet());

    return new UserResponse(
        u.getId(),
        u.getPhone(),
        u.getFullName(),
        u.getDob(),
        u.getRoles(),
        u.isEnabled(),
        u.isActive(),
        u.getCreatedAt(),
        u.getUpdatedAt(),
        addrResponses);
  }

  @Transactional()
  public PageResult<UserResponse> listUsers(
      String q,
      Role role,
      Boolean enabled,
      Boolean isActive,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      int page,
      int size,
      Sort sort) {
    Pageable pageable = PageRequest.of(page, size, sort);

    Specification<User> spec =
        UserSpecifications.build(q, role, enabled, isActive, createdFrom, createdTo);

    Page<User> p = userRepo.findAll(spec, pageable);

    List<UserResponse> items = p.stream().map(this::toResponse).collect(Collectors.toList());

    return new PageResult<>(
        items, p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
  }

  @Transactional
  public UserAddressResponse addAddressForUser(UUID userId, UserAddressCreateRequest req) {
    User user =
        userRepo.findById(userId).orElseThrow(() -> AppException.notFound("User not found"));

    // Validate lat/long ranges
    BigDecimal lat = req.latitude();
    BigDecimal lon = req.longitude();

    if (lat != null) {
      if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
        throw AppException.badRequest("latitude must be between -90 and 90");
      }
    }
    if (lon != null) {
      if (lon.compareTo(BigDecimal.valueOf(-180)) < 0
          || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
        throw AppException.badRequest("longitude must be between -180 and 180");
      }
    }

    // Lấy danh sách addresses hiện có (để kiểm tra primary và quyết định auto-primary)
    List<UserAddress> existing = addressRepo.findByUserId(userId);

    UserAddress a = new UserAddress();
    a.setUser(user);
    a.setAddressLine(trimOrNull(req.addressLine()));
    a.setWard(trimOrNull(req.ward()));
    a.setDistrict(trimOrNull(req.district()));
    a.setProvince(trimOrNull(req.province()));
    a.setCountry(req.country() == null ? "VN" : req.country().trim());
    a.setLatitude(lat);
    a.setLongitude(lon);
    a.setNote(trimOrNull(req.note()));

    boolean hasExisting = !existing.isEmpty();

    // Business rule:
    // - Nếu chưa có address nào -> auto set primary = true
    // - Nếu đã có và req.isPrimary == true -> unset others and set this primary
    // - Nếu đã có và req.isPrimary == null/false -> set false
    boolean requestedPrimary = Boolean.TRUE.equals(req.isPrimary());
    if (!hasExisting) {
      a.setPrimary(true);
    } else if (requestedPrimary) {
      // unset all existing primary flags
      for (UserAddress ex : existing) {
        if (ex.isPrimary()) {
          ex.setPrimary(false);
          addressRepo.save(ex);
        }
      }
      a.setPrimary(true);
    } else {
      a.setPrimary(false);
    }

    UserAddress saved = addressRepo.save(a);

    return toResponse(saved);
  }

  @Transactional
  public void removeAddressForUser(UUID userId, UUID addressId) {
    UserAddress a =
        addressRepo
            .findById(addressId)
            .orElseThrow(() -> AppException.notFound("Address not found"));

    if (!a.getUser().getId().equals(userId)) {
      throw AppException.forbidden("Address does not belong to user");
    }

    // Business rule: không cho xóa address primary
    if (a.isPrimary()) {
      throw AppException.badRequest(
          "Cannot delete primary address. Set another address as primary first.");
    }

    addressRepo.delete(a);
  }

  @Transactional
  public void removeAddressForUserByAdmin(UUID userId, UUID addressId) {
    UserAddress a =
        addressRepo
            .findById(addressId)
            .orElseThrow(() -> AppException.notFound("Address not found"));

    if (!a.getUser().getId().equals(userId)) {
      throw AppException.forbidden("Address does not belong to user");
    }

    if (a.isPrimary()) {
      throw AppException.badRequest("Cannot delete primary address");
    }

    addressRepo.delete(a);
  }

  @Transactional
  public UserAddressResponse updateAddressForUser(
      UUID userId, UUID addressId, UserAddressUpdateRequest req) {
    // load and check ownership
    UserAddress a =
        addressRepo
            .findById(addressId)
            .orElseThrow(() -> AppException.notFound("Address not found"));

    if (!a.getUser().getId().equals(userId)) {
      throw AppException.forbidden("Address does not belong to user");
    }

    // validate lat/long ranges (BigDecimal)
    BigDecimal lat = req.latitude();
    BigDecimal lon = req.longitude();

    if (lat != null) {
      if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
        throw AppException.badRequest("latitude must be between -90 and 90");
      }
    }
    if (lon != null) {
      if (lon.compareTo(BigDecimal.valueOf(-180)) < 0
          || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
        throw AppException.badRequest("longitude must be between -180 and 180");
      }
    }

    // Update allowed fields if provided (null = không thay đổi)
    if (req.addressLine() != null) a.setAddressLine(trimOrNull(req.addressLine()));
    if (req.ward() != null) a.setWard(trimOrNull(req.ward()));
    if (req.district() != null) a.setDistrict(trimOrNull(req.district()));
    if (req.province() != null) a.setProvince(trimOrNull(req.province()));
    if (req.country() != null) a.setCountry(trimOrNull(req.country()));
    if (req.latitude() != null) a.setLatitude(req.latitude());
    if (req.longitude() != null) a.setLongitude(req.longitude());
    if (req.note() != null) a.setNote(trimOrNull(req.note()));

    boolean requestPrimary = Boolean.TRUE.equals(req.isPrimary());

    if (requestPrimary) {
      // unset others
      List<UserAddress> existing = addressRepo.findByUserId(userId);
      for (UserAddress ex : existing) {
        if (ex.isPrimary() && !ex.getId().equals(a.getId())) {
          ex.setPrimary(false);
          addressRepo.save(ex);
        }
      }
      a.setPrimary(true);
    } else if (req.isPrimary() != null) {
      // explicit set false requested
      // If user tries to unset primary on the only primary address, we allow unset only if there is
      // another primary set
      // but per your rules we probably should not allow removing primary without choosing another;
      // so disallow unsetting primary directly.
      if (a.isPrimary()) {
        throw AppException.badRequest(
            "Cannot unset primary address directly. Set another address as primary first.");
      }
      a.setPrimary(false);
    }
    // else: req.isPrimary() == null -> keep current primary flag

    UserAddress saved = addressRepo.save(a);
    return toResponse(saved);
  }

  // Admin variant (admin can update any user's address)
  @Transactional
  public UserAddressResponse updateAddressForUserByAdmin(
      UUID userId, UUID addressId, UserAddressUpdateRequest req) {
    // confirm address belongs to userId
    UserAddress a =
        addressRepo
            .findById(addressId)
            .orElseThrow(() -> AppException.notFound("Address not found"));

    if (!a.getUser().getId().equals(userId)) {
      throw AppException.badRequest("Address does not belong to provided userId");
    }

    // reuse same logic
    return updateAddressForUser(userId, addressId, req);
  }

  // Helper: set 1 address làm primary (dùng khi muốn thay primary)
  @Transactional
  public UserAddressResponse setPrimaryAddress(UUID userId, UUID addressId) {
    User user =
        userRepo.findById(userId).orElseThrow(() -> AppException.notFound("User not found"));

    UserAddress toPrimary =
        addressRepo
            .findById(addressId)
            .orElseThrow(() -> AppException.notFound("Address not found"));

    if (!toPrimary.getUser().getId().equals(userId)) {
      throw AppException.forbidden("Address does not belong to user");
    }

    // unset others
    addressRepo
        .findByUserId(userId)
        .forEach(
            addr -> {
              if (addr.isPrimary()) {
                addr.setPrimary(false);
                addressRepo.save(addr);
              }
            });

    toPrimary.setPrimary(true);
    UserAddress saved = addressRepo.save(toPrimary);
    return toResponse(saved);
  }

  private UserAddressResponse toResponse(UserAddress a) {
    return new UserAddressResponse(
        a.getId(),
        a.getAddressLine(),
        a.getWard(),
        a.getDistrict(),
        a.getProvince(),
        a.getCountry(),
        a.getLatitude(),
        a.getLongitude(),
        a.getNote(),
        a.isPrimary(),
        a.getCreatedAt(),
        a.getUpdatedAt());
  }

  private String trimOrNull(String s) {
    return s == null ? null : s.trim();
  }
}
