package com.pbl6.auth.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UserAddressCreateRequest(
    @Size(max = 500) String addressLine,
    @Size(max = 150) String ward,
    @Size(max = 150) String district,
    @Size(max = 150) String province,
    @Size(max = 100) String country,
    BigDecimal latitude,
    BigDecimal longitude,
    @Size(max = 300) String detail,
    @Size(max = 300) String note,
    Boolean isPrimary) {}
