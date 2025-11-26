package com.pbl6.auth.controller;

import com.pbl6.auth.dto.DriverRegisterRequest;
import com.pbl6.auth.dto.DriverResponse;
import com.pbl6.auth.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
