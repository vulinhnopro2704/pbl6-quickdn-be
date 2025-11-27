package com.pbl6.auth.controller;

import com.pbl6.auth.dto.UserResponse;
import com.pbl6.auth.dto.UserUpdateRequest;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
