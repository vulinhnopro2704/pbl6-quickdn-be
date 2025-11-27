package com.pbl6.auth.service;

import com.pbl6.auth.dto.DriverRegisterRequest;
import com.pbl6.auth.dto.DriverResponse;
import com.pbl6.auth.entity.DriverEntity;
import com.pbl6.auth.entity.DriverStatus;
import com.pbl6.auth.entity.Role;
import com.pbl6.auth.entity.User;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.DriverRepository;
import com.pbl6.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
