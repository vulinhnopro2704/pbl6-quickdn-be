package com.pbl6.order.repository;

import com.pbl6.order.entity.OrderPriceRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderPriceRouteRepository extends JpaRepository<OrderPriceRouteEntity, UUID> {

    /**
     * Fetch price routes for many orders in one query.
     * join fetch pr.order to avoid lazy-loading pr.getOrder() later.
     */
    @Query("""
              select pr
              from OrderPriceRouteEntity pr
              join fetch pr.order o
              where o.id in :ids
            """)
    List<OrderPriceRouteEntity> findByOrderIds(@Param("ids") List<UUID> ids);
}
