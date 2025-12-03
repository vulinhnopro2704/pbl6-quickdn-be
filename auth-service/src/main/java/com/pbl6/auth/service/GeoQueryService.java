package com.pbl6.auth.service;

import com.pbl6.auth.constant.RedisKeyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeoQueryService {

  private final RedisTemplate<String, String> redisTemplate;

  @Autowired
  public GeoQueryService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /** return list of driverId strings (UUID as string) */
  public List<String> findNearby(double pickupLat, double pickupLon, double radiusKm, int limit) {
    Point point = new Point(pickupLon, pickupLat);
    Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);

    GeoResults<RedisGeoCommands.GeoLocation<String>> results =
        redisTemplate
            .opsForGeo()
            .radius(
                RedisKeyConstants.DRIVERS_GEO_KEY,
                new Circle(point, distance),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending()
                    .limit(limit));

    if (results == null) return List.of();
    return results.getContent().stream()
        .map(gr -> gr.getContent().getName()) // driverId as string
        .collect(Collectors.toList());
  }
}
