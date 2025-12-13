package com.pbl6.order.repository;

import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.repository.projection.OrderStatusCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
    /**
     * Fetch orders + packages + package.dropoffAddress + order.pickupAddress for a list of ids.
     * Use distinct to avoid duplicates due to join.
     */
    @Query("""
    select distinct o
    from OrderEntity o
      left join fetch o.packages p
      left join fetch p.dropoffAddress
      left join fetch o.pickupAddress
    where o.id in :ids
  """)
    List<OrderEntity> findAllByIdInWithPackages(@Param("ids") List<UUID> ids);

    @Query(
        """
            select o.status as status, count(o) as count
            from OrderEntity o
            where (:fromDate is null or o.createdAt >= :fromDate)
              and (:toDate is null or o.createdAt <= :toDate)
              and (:districtCode is null or o.pickupAddress.districtCode = :districtCode)
            group by o.status
        """)
    List<OrderStatusCountProjection> aggregateStatusCounts(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("districtCode") Integer districtCode);
}
