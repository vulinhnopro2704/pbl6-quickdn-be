package com.pbl6.auth.service;

import com.pbl6.auth.dto.UserResponse;
import com.pbl6.auth.dto.UserUpdateRequest;
import com.pbl6.auth.entity.User;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepo;

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
    return new UserResponse(
        u.getId(),
        u.getPhone(),
        u.getFullName(),
        u.getDob(),
        u.getRoles(),
        u.isEnabled(),
        u.isActive(),
        u.getCreatedAt(),
        u.getUpdatedAt());
  }
}
