package com.pbl6.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UserAddressCreateRequest(
    @Size(max = 500) String addressLine,
    @Size(max = 500) String description,
    @NotBlank(message = "detail is required") @Size(max = 500) String detail,
    @Size(max = 150) String name,
    @Size(max = 20) String phone,
    @NotNull(message = "latitude is required") BigDecimal latitude,
    @NotNull(message = "longitude is required") BigDecimal longitude,
    Integer wardCode,
    Integer districtCode,
    @Size(max = 300) String note,
    Boolean isPrimary) {}
