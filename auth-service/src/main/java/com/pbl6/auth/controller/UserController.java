package com.pbl6.auth.controller;

import com.pbl6.auth.dto.*;
import com.pbl6.auth.entity.Role;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.service.UserService;
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

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  @Operation(summary = "Get User by ID")
  public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  // PUT /api/auth/users/{id} -- cập nhật bởi admin hoặc hệ thống
  @PutMapping("/{id}")
  @Operation(summary = "Update User by ID (Admin only)")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<UserResponse> updateUserById(
      @PathVariable UUID id, @RequestBody @Valid UserUpdateRequest req) {

    return ResponseEntity.ok(userService.updateById(id, req));
  }

  // PUT /api/auth/users/me -- cập nhật chính mình
  @PutMapping("/me")
  @Operation(summary = "Update Self User")
  public ResponseEntity<UserResponse> updateMe(
      Authentication auth, @RequestBody @Valid UserUpdateRequest req) {

    UUID userId;
    try {
      userId = UUID.fromString(auth.getName());
    } catch (Exception ex) {
      throw AppException.forbidden("Invalid authentication principal");
    }

    return ResponseEntity.ok(userService.updateSelf(userId, req));
  }

  @Operation(summary = "Get List Users with Filters (Admin only)")
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public PageResult<UserResponse> listUsers(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "role", required = false) Role role,
      @RequestParam(name = "enabled", required = false) Boolean enabled,
      @RequestParam(name = "isActive", required = false) Boolean isActive,
      @RequestParam(name = "createdFrom", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime createdFrom,
      @RequestParam(name = "createdTo", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime createdTo,
      @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(name = "size", defaultValue = "50") @Min(1) int size,
      @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sortStr) {
    int pageIndex = page - 1;
    Sort sort = parseSort(sortStr);
    return userService.listUsers(
        q, role, enabled, isActive, createdFrom, createdTo, pageIndex, size, sort);
  }

  private Sort parseSort(String sortStr) {
    // Spring style: property,(asc|desc). Allow comma-separated multiple sorts.
    if (sortStr == null || sortStr.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }
    String[] parts = sortStr.split(",");
    if (parts.length >= 2) {
      String prop = parts[0].trim();
      String dir = parts[1].trim().equalsIgnoreCase("asc") ? "asc" : "desc";
      return Sort.by(Sort.Direction.fromString(dir), prop);
    }
    return Sort.by(Sort.Direction.DESC, sortStr);
  }

  // --- User self: add address ---
  @Operation(summary = "Add User Addresses (Self)")
  @PostMapping("/me/addresses")
  public ResponseEntity<UserAddressResponse> addAddressMe(
      Principal principal, @RequestBody @Valid UserAddressCreateRequest req) {

    UUID userId = parseUserIdFromPrincipal(principal);
    UserAddressResponse resp = userService.addAddressForUser(userId, req);

    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  // --- User self: remove address ---
  @Operation(summary = "Remove User Address (Self)")
  @DeleteMapping("/me/addresses/{addressId}")
  public ResponseEntity<Void> removeAddressMe(Principal principal, @PathVariable UUID addressId) {

    UUID userId = parseUserIdFromPrincipal(principal);
    userService.removeAddressForUser(userId, addressId);
    return ResponseEntity.noContent().build();
  }

  // --- Admin: add address for any user ---
  @Operation(summary = "Add User Addresses for any User (Admin only)")
  @PostMapping("/{userId}/addresses")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<UserAddressResponse> addAddressForUserByAdmin(
      @PathVariable UUID userId, @RequestBody @Valid UserAddressCreateRequest req) {

    UserAddressResponse resp = userService.addAddressForUser(userId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  // --- Admin: remove address for any user ---
  @Operation(summary = "Remove User Address for any User (Admin only)")
  @DeleteMapping("/{userId}/addresses/{addressId}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> removeAddressForUserByAdmin(
      @PathVariable UUID userId, @PathVariable UUID addressId) {

    userService.removeAddressForUser(userId, addressId);
    return ResponseEntity.noContent().build();
  }

  private UUID parseUserIdFromPrincipal(Principal principal) {
    try {
      return UUID.fromString(principal.getName());
    } catch (Exception ex) {
      throw AppException.forbidden("Invalid principal");
    }
  }

  // --- User self: update address ---
  @PutMapping("/me/addresses/{addressId}")
  @Operation(summary = "Update User Address (Self)")
  public ResponseEntity<UserAddressResponse> updateAddressMe(
      Principal principal,
      @PathVariable UUID addressId,
      @RequestBody @Valid UserAddressUpdateRequest req) {

    UUID userId = parseUserIdFromPrincipal(principal);
    UserAddressResponse resp = userService.updateAddressForUser(userId, addressId, req);
    return ResponseEntity.ok(resp);
  }

  // --- Admin: update address for any user ---
  @PutMapping("/{userId}/addresses/{addressId}")
  @PreAuthorize("hasAuthority('ADMIN')")
  @Operation(summary = "Update User Address for any User (Admin only)")
  public ResponseEntity<UserAddressResponse> updateAddressForUserByAdmin(
      @PathVariable UUID userId,
      @PathVariable UUID addressId,
      @RequestBody @Valid UserAddressUpdateRequest req) {

    UserAddressResponse resp = userService.updateAddressForUserByAdmin(userId, addressId, req);
    return ResponseEntity.ok(resp);
  }

    @PostMapping("/update-fcmtoken")
    @Operation(summary = "User Update FCM Token")
    public ResponseEntity<?> updateStatus(Authentication auth, @RequestBody UpdateFcmTokenRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        userService.updateFcmToken(userId, request);
        return ResponseEntity.ok("Success");
    }
}
