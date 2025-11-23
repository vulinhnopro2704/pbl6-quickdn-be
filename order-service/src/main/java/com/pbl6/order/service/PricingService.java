package com.pbl6.order.service;

import com.pbl6.order.entity.PackageSize;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {
  // Simple example; you can replace with real algorithm or external API
  public double estimateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
    // Haversine
    double R = 6371; // km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  public double calcDeliveryFee(PackageSize size, double distanceKm) {
    double perKm =
        switch (size) {
          case S -> 3000;
          case M -> 4000;
          case L -> 6000;
          case XL -> 9000;
        };
    return Math.round(distanceKm * perKm);
  }
}
