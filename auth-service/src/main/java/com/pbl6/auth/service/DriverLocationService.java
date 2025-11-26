package com.pbl6.auth.service;

import com.pbl6.auth.dto.DriverLocationResponse;
import com.pbl6.auth.entity.DriverLocationLatest;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.DriverLocationLatestRepository;
import com.pbl6.auth.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverLocationService {

  private final DriverLocationLatestRepository repo;
  private final DriverRepository driverRepo;

  @Transactional
  public DriverLocationResponse updateLocation(UUID actor, UUID driverId, double lat, double lon) {
    var driver =
        driverRepo.findByUserId(driverId).orElseThrow(() -> AppException.notFound("Driver not found"));

    if (!driver.getUserId().equals(actor)) {
        throw AppException.forbidden("Cannot update other driver's location");
    }

    var latBd = BigDecimal.valueOf(lat);
    var lonBd = BigDecimal.valueOf(lon);
    var now = LocalDateTime.now();

    DriverLocationLatest locationLatest = repo.findByDriverId(driverId).orElse(null);
    if (locationLatest == null) {
        locationLatest = new DriverLocationLatest();
        locationLatest.setDriverId(driverId);
    }
    locationLatest.setLatitude(latBd);
    locationLatest.setLongitude(lonBd);
    locationLatest.setRecordedAt(now);
    locationLatest.setUpdatedAt(now);
    repo.save(locationLatest);

    return new DriverLocationResponse(
        locationLatest.getDriverId(),
        locationLatest.getLatitude().doubleValue(),
        locationLatest.getLongitude().doubleValue(),
        locationLatest.getRecordedAt(),
        locationLatest.getUpdatedAt());
  }

  public Optional<DriverLocationResponse> getLatest(UUID driverId) {
    return repo.findByDriverId(driverId)
        .map(
            e ->
                new DriverLocationResponse(
                    e.getDriverId(),
                    e.getLatitude().doubleValue(),
                    e.getLongitude().doubleValue(),
                    e.getRecordedAt(),
                    e.getUpdatedAt()));
  }
}
