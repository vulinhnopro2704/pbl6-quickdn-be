package com.pbl6.order.repository;

import com.pbl6.order.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {
}