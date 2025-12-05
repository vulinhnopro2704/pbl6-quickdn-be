package com.pbl6.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDriverRequest(@NotNull UUID orderId) {}
