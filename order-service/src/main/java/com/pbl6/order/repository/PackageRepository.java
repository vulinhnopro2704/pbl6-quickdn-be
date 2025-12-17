package com.pbl6.order.repository;

import com.pbl6.order.entity.PackageEntity;
import com.pbl6.order.repository.projection.PackageStatusCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {

	@Query(
			"""
				select p.status as status, count(p) as count
				from PackageEntity p
				left join p.order o
				left join p.dropoffAddress dropoff
				where p.createdAt >= coalesce(:fromDate, p.createdAt)
					and p.createdAt <= coalesce(:toDate, p.createdAt)
					and dropoff.districtCode = coalesce(:districtCode, dropoff.districtCode)
				group by p.status
			""")
	List<PackageStatusCountProjection> aggregateStatusCounts(
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			@Param("districtCode") Integer districtCode);
}
