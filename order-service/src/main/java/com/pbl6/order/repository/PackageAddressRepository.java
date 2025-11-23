package com.pbl6.order.repository;

import com.pbl6.order.entity.PackageAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageAddressRepository extends JpaRepository<PackageAddressEntity, UUID> {}
