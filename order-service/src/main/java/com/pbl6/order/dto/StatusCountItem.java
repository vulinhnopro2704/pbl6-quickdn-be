package com.pbl6.order.dto;

public record StatusCountItem(int statusCode, String statusKey, String statusLabel, String colorCode, long count) {}
