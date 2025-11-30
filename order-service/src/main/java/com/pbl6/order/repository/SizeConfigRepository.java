package com.pbl6.order.repository;

import com.pbl6.order.entity.PackageSize;
import com.pbl6.order.entity.SizeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SizeConfigRepository extends JpaRepository<SizeConfig, Long> {
  Optional<SizeConfig> findBySizeCode(PackageSize sizeCode);
}
