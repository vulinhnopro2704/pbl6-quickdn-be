package com.pbl6.order.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available package sizes")
public enum PackageSize {
  S,
  M,
  L,
  XL
}
