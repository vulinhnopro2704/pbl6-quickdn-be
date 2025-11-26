package com.pbl6.auth.repository;

import com.pbl6.auth.entity.DriverLocationLatest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DriverLocationLatestRepository extends JpaRepository<DriverLocationLatest, UUID> {

    Optional<DriverLocationLatest> findByDriverId(UUID driverId);
}
