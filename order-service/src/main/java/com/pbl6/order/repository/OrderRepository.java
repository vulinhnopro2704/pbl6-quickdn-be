package com.pbl6.order.repository;

import com.pbl6.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository
    extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {
  List<OrderEntity> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    @Query("""
        select o.shipperId
        from OrderEntity o
        join o.packages p
        where p.id = :packageId
    """)
    Optional<UUID> findShipperIdByPackageId(@Param("packageId") UUID packageId);
}
