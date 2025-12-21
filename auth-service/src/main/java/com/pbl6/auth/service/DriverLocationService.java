package com.pbl6.auth.service;

import com.pbl6.auth.constant.RedisKeyConstants;
import com.pbl6.auth.dto.DriverLocationRequest;
import com.pbl6.auth.dto.DriverLocationResponse;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.util.GeoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.pbl6.auth.constant.RedisKeyConstants.DRIVER_HASH_PREFIX;
import static com.pbl6.auth.constant.RedisKeyConstants.DRIVER_FCM_TOKEN;

@Slf4j
@Service
public class DriverLocationService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final int TTL_SECONDS = 60;
  private static final double MIN_MOVE_METERS = 20.0;
  private static final long MIN_INTERVAL_MS = 3000L;

  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Autowired
  public DriverLocationService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean updateLocation(DriverLocationRequest dto, UUID driverId) {
    String driverFcmTokenKey = String.format(DRIVER_FCM_TOKEN, driverId);
    Boolean isReady = redisTemplate.hasKey(driverFcmTokenKey);
    if (!isReady) {
      throw AppException.badRequest("Driver is not available for location update");
    }

    String driverKey = DRIVER_HASH_PREFIX + driverId;

    // 1. Check previous location for distance & time threshold
    Map<Object, Object> prev = redisTemplate.opsForHash().entries(driverKey);
    if (!prev.isEmpty()) {
      try {
        String prevLatStr = (String) prev.get("lat");
        String prevLonStr = (String) prev.get("lon");
        String prevTsStr = (String) prev.get("updatedAt");

        if (prevLatStr != null && prevLonStr != null && prevTsStr != null) {
          double prevLat = Double.parseDouble(prevLatStr);
          double prevLon = Double.parseDouble(prevLonStr);
          LocalDateTime prevTime = LocalDateTime.parse(prevTsStr, formatter);

          long nowEpoch = System.currentTimeMillis();
          long prevEpoch = prevTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
          long diffMs = nowEpoch - prevEpoch;

          double distance =
              GeoUtils.haversineDistanceMeters(
                  prevLat, prevLon, dto.latitude().doubleValue(), dto.longitude().doubleValue());

          if (distance < MIN_MOVE_METERS && diffMs < MIN_INTERVAL_MS) {
            return false; // skip insignificant update
          }
        }
      } catch (Exception ex) {
        log.warn(
            "Error parsing previous Redis location data for driver {}: {}",
            driverId,
            ex.getMessage());
      }
    }

    // 2. Update Redis GEO for spatial search
    redisTemplate
        .opsForGeo()
        .add(
            RedisKeyConstants.DRIVERS_GEO_KEY,
            new Point(dto.longitude().doubleValue(), dto.latitude().doubleValue()),
            driverId.toString());

    // 3. Update Redis hash with metadata
    Map<String, String> map = new HashMap<>();
    map.put("lat", dto.latitude().toString());
    map.put("lon", dto.longitude().toString());
    map.put("updatedAt", LocalDateTime.now().format(formatter));
    redisTemplate.opsForHash().putAll(driverKey, map);
    return true;
  }

  public Optional<DriverLocationResponse> getLatestLocation(UUID driverId) {
    String driverKey = DRIVER_HASH_PREFIX + driverId;
    Map<Object, Object> data = redisTemplate.opsForHash().entries(driverKey);
    if (data.isEmpty()) return Optional.empty();

    try {
      String latStr = (String) data.get("lat");
      String lonStr = (String) data.get("lon");
      String tsStr = (String) data.get("updatedAt");

      if (latStr != null && lonStr != null && tsStr != null) {
        BigDecimal latitude = new BigDecimal(latStr);
        BigDecimal longitude = new BigDecimal(lonStr);
        LocalDateTime updatedAt = LocalDateTime.parse(tsStr, formatter);
        return Optional.of(new DriverLocationResponse(driverId, latitude, longitude, updatedAt));
      }
    } catch (Exception e) {
      log.error("Error parsing location data for driver {}", driverId, e);
    }

    return Optional.empty();
  }
}
