package com.pbl6.order.dto;

import com.pbl6.order.entity.PackageStatus;
import jakarta.validation.constraints.NotNull;

public record PackageStatusUpdateRequest(
        @NotNull PackageStatus status,
        String note
) {}
