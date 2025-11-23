package com.pbl6.order.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Who pays for the delivery")
public enum PayerType {
  SENDER,
  RECEIVER
}
