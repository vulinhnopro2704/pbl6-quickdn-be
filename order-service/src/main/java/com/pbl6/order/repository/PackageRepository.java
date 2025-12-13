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
					join p.dropoffAddress d
					where (:fromDate is null or p.createdAt >= :fromDate)
						and (:toDate is null or p.createdAt <= :toDate)
						and (:districtCode is null or d.districtCode = :districtCode)
					group by p.status
			""")
	List<PackageStatusCountProjection> aggregateStatusCounts(
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			@Param("districtCode") Integer districtCode);
}
