package com.pbl6.microservices.auth.repository;

import com.pbl6.microservices.auth.entity.RefreshToken;
import com.pbl6.microservices.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);   // thêm
    void deleteByUser(User user);
}
