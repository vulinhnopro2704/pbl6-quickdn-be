package com.pbl6.auth.dto;

import java.math.BigDecimal;

public record DriverLocationRequest(BigDecimal latitude, BigDecimal longitude) {}
