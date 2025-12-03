package com.pbl6.auth.controller;

import com.pbl6.auth.dto.DriverLocationRequest;
import com.pbl6.auth.dto.DriverLocationResponse;
import com.pbl6.auth.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverLocationController {

  private final DriverLocationService locationService;

  @PutMapping("/location")
  @PreAuthorize("hasAuthority('DRIVER')")
  public ResponseEntity<?> updateLocation(
      @RequestBody DriverLocationRequest req, Authentication auth) {

    UUID driverId = UUID.fromString(auth.getName());
    boolean resp = locationService.updateLocation(req, driverId);
    if (!resp) {
      return ResponseEntity.badRequest().body("Failed to update location");
    }
    return ResponseEntity.ok("Location updated successfully");
  }

  @GetMapping("/{driverId}/location")
  public ResponseEntity<DriverLocationResponse> getLatest(@PathVariable UUID driverId) {
    Optional<DriverLocationResponse> opt = locationService.getLatestLocation(driverId);
    return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
  }


}
