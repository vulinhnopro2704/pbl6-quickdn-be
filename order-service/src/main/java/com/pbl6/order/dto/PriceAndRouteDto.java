package com.pbl6.order.dto;

public record PriceAndRouteDto(
    double price,
    double latitude,
    double longitude,
    int routeIndex,
    int packageIndex,
    int distance,
    int estimatedDuration) {}
