package com.pbl6.auth.controller;

import com.pbl6.auth.dto.*;
import com.pbl6.auth.entity.DriverStatus;
import com.pbl6.auth.entity.Gender;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

  private final DriverService driverService;

  @Operation(summary = "Driver register")
  @PostMapping
  public ResponseEntity<DriverResponse> register(
      @Valid @RequestBody DriverRegisterRequest req, Authentication auth) {
    UUID userId = UUID.fromString(auth.getName());
    DriverResponse resp = driverService.register(req, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @Operation(summary = "Get Driver")
  @GetMapping("/{id}")
  public ResponseEntity<DriverResponse> get(@PathVariable("id") UUID userId) {
    return ResponseEntity.ok(driverService.get(userId));
  }

  @Operation(summary = "Approve or Reject Driver (Admin only)")
  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<DriverResponse> approve(
      @PathVariable UUID id, @RequestParam boolean approve) {

    return ResponseEntity.ok(driverService.approve(id, approve));
  }

  @GetMapping
  @Operation(summary = "Get List of Drivers with filters (Admin only)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public PageResult<DriverListItemResponse> listDrivers(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) DriverStatus status,
      @RequestParam(required = false) Gender gender,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime createdFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime createdTo,
      @RequestParam(required = false) Integer ratingMin,
      @RequestParam(required = false) Integer ratingMax,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    int pageIndex = page - 1;

    Sort sortObj = parseSort(sort);

    return driverService.listDrivers(
        q, status, gender, createdFrom, createdTo, ratingMin, ratingMax, pageIndex, size, sortObj);
  }

  @PostMapping("/update-status")
  @PreAuthorize("hasAuthority('DRIVER')")
  @Operation(summary = "Driver update status (online/offline)")
  public ResponseEntity<?> updateStatus(
      Authentication auth, @RequestBody UpdateStatus status) {
    UUID driverId = UUID.fromString(auth.getName());
    driverService.updateStatus(driverId, status);
    return ResponseEntity.ok("Success");
  }

  private Sort parseSort(String input) {
    String[] parts = input.split(",");
    if (parts.length == 1) return Sort.by(parts[0]).ascending();
    return parts[1].equalsIgnoreCase("desc")
        ? Sort.by(parts[0]).descending()
        : Sort.by(parts[0]).ascending();
  }
}
