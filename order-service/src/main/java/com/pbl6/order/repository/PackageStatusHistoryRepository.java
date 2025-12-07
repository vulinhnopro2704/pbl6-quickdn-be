package com.pbl6.order.repository;

import com.pbl6.order.entity.PackageStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageStatusHistoryRepository extends JpaRepository<PackageStatusHistory, UUID> {
}
