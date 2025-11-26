package com.pbl6.auth.repository;

import com.pbl6.auth.entity.DriverEntity;
import com.pbl6.auth.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<DriverEntity, UUID> {
    Optional<DriverEntity> findByUserId(UUID userId);
    Optional<DriverEntity> findByVehiclePlateNumber(String vehiclePlateNumber);
}
