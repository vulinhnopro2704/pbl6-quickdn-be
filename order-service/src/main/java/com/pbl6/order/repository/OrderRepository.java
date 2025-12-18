package com.pbl6.order.repository;

import com.pbl6.order.entity.OrderEntity;
import com.pbl6.order.repository.projection.OrderStatusCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query("select o.orderCode from OrderEntity o where o.id = :id")
    Long findOrderCodeById(UUID id);

    OrderEntity findOrderEntitiesByOrderCode(Long orderCode);

    @Query(
        """
          select o.status as status, count(o) as count
          from OrderEntity o
          where o.createdAt >= coalesce(:fromDate, o.createdAt)
            and o.createdAt <= coalesce(:toDate, o.createdAt)
            and o.pickupAddress.districtCode = coalesce(:districtCode, o.pickupAddress.districtCode)
          group by o.status
        """)
    List<OrderStatusCountProjection> aggregateStatusCounts(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("districtCode") Integer districtCode);

    @Query("select count(o) from OrderEntity o")
    long countTotalOrders();

    @Query(
      """
        select count(distinct o)
        from OrderEntity o
        left join o.packages p on p.status = com.pbl6.order.entity.PackageStatus.WAITING_FOR_PICKUP
        where o.status in :pendingStatuses or p.id is not null
      """)
    long countPendingOrders(@Param("pendingStatuses") List<com.pbl6.order.entity.OrderStatus> pendingStatuses);

    @Query(
      "select coalesce(sum(o.totalAmount),0) from OrderEntity o where o.status in :completedStatuses")
    BigDecimal sumTotalRevenue(@Param("completedStatuses") List<com.pbl6.order.entity.OrderStatus> completedStatuses);

    @Query(
      """
        select coalesce(sum(o.totalAmount),0)
        from OrderEntity o
        where o.status in :completedStatuses and o.createdAt >= :from and o.createdAt < :to
      """)
    BigDecimal sumRevenueBetween(
      @Param("completedStatuses") List<com.pbl6.order.entity.OrderStatus> completedStatuses,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);
}
