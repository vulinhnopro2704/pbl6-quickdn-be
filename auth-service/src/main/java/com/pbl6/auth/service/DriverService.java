package com.pbl6.auth.service;

import com.pbl6.auth.dto.DriverListItemResponse;
import com.pbl6.auth.dto.DriverRegisterRequest;
import com.pbl6.auth.dto.DriverResponse;
import com.pbl6.auth.dto.PageResult;
import com.pbl6.auth.entity.*;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.DriverRepository;
import com.pbl6.auth.repository.UserRepository;
import com.pbl6.auth.spec.DriverSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

  private final DriverRepository driverRepo;
  private final UserRepository userRepository;

  @Transactional
  public DriverResponse register(DriverRegisterRequest req, UUID userId) {

    // Kiểm tra biển số xe đã tồn tại
    driverRepo
        .findByVehiclePlateNumber(req.vehiclePlateNumber().trim())
        .ifPresent(
            d -> {
              throw AppException.conflict("vehiclePlateNumber already exists");
            });

    DriverEntity d = new DriverEntity();
    d.setUserId(userId);

    // Chuẩn hóa biển số xe (VD: AB-1234 → AB1234)
    d.setVehiclePlateNumber(req.vehiclePlateNumber().trim().toUpperCase());

    d.setLicenseNumber(req.licenseNumber().trim());

    d.setIdentityFullName(trimOrNull(req.identityFullName()));
    d.setIdentityNumber(trimOrNull(req.identityNumber()));
    d.setIdentityIssueDate(req.identityIssueDate());
    d.setIdentityIssuePlace(trimOrNull(req.identityIssuePlace()));
    d.setIdentityAddress(trimOrNull(req.identityAddress()));
    d.setIdentityGender(req.identityGender());
    d.setIdentityBirthdate(req.identityBirthdate());

    d.setStatus(DriverStatus.PENDING);

    d = driverRepo.save(d);
    return toResponse(d);
  }

  private String trimOrNull(String s) {
    return s == null ? null : s.trim();
  }

  public DriverResponse get(UUID id) {
    DriverEntity e =
        driverRepo.findByUserId(id).orElseThrow(() -> AppException.notFound("Driver not found"));
    return toResponse(e);
  }

  @Transactional
  public DriverResponse approve(UUID driverId, boolean approve) {
    DriverEntity e =
        driverRepo
            .findByUserId(driverId)
            .orElseThrow(() -> AppException.notFound("Driver not found"));
    if (approve) {
      e.setStatus(DriverStatus.APPROVED);
      e.setApprovedAt(LocalDateTime.now());
      User user =
          userRepository
              .findById(driverId)
              .orElseThrow(() -> AppException.notFound("Driver not found"));
      user.getRoles().add(Role.DRIVER);
      userRepository.save(user);
    } else {
      e.setStatus(DriverStatus.REJECTED);
    }
    e = driverRepo.save(e);
    return toResponse(e);
  }

  public PageResult<DriverListItemResponse> listDrivers(
      String q,
      DriverStatus status,
      Gender gender,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      Integer ratingMin,
      Integer ratingMax,
      int page,
      int size,
      Sort sort) {
    Pageable pageable = PageRequest.of(page, size, sort);

    List<Specification<DriverEntity>> specs = new ArrayList<>();

    if (q != null && !q.isBlank()) specs.add(DriverSpecifications.search(q));
    if (status != null) specs.add(DriverSpecifications.hasStatus(status));
    if (gender != null) specs.add(DriverSpecifications.hasGender(gender));
    if (createdFrom != null) specs.add(DriverSpecifications.createdFrom(createdFrom));
    if (createdTo != null) specs.add(DriverSpecifications.createdTo(createdTo));
    if (ratingMin != null) specs.add(DriverSpecifications.ratingMin(ratingMin));
    if (ratingMax != null) specs.add(DriverSpecifications.ratingMax(ratingMax));

    Specification<DriverEntity> finalSpec = specs.stream().reduce(Specification::and).orElse(null);

    Page<DriverEntity> p = driverRepo.findAll(finalSpec, pageable);

    return new PageResult<>(
        p.map(this::toListItem).toList(), p.getTotalElements(), p.getTotalPages(), page + 1, size);
  }

  private DriverListItemResponse toListItem(DriverEntity d) {
    return new DriverListItemResponse(
        d.getId(),
        d.getUserId(),
        d.getVehiclePlateNumber(),
        d.getLicenseNumber(),
        d.getIdentityFullName(),
        d.getIdentityGender(),
        d.getStatus(),
        d.getRatingAvg(),
        d.getRatingCount(),
        d.getCreatedAt(),
        d.getUpdatedAt());
  }

  private DriverResponse toResponse(DriverEntity e) {
    return new DriverResponse(
        e.getId(),
        e.getUserId(),
        e.getVehiclePlateNumber(),
        e.getLicenseNumber(),
        e.getIdentityFullName(),
        e.getIdentityNumber(),
        e.getIdentityIssueDate(),
        e.getIdentityIssuePlace(),
        e.getIdentityAddress(),
        e.getIdentityGender(),
        e.getIdentityBirthdate(),
        e.getStatus(),
        e.getRatingAvg(),
        e.getRatingCount(),
        e.getApprovedAt(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
