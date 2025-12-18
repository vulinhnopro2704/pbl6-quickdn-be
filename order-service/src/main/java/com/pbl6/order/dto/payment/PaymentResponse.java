package com.pbl6.order.dto.payment;

import com.pbl6.order.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for our backend API Returns local payment ID + payOS checkout details */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PaymentResponse", description = "Payment creation response with checkout URL")
public class PaymentResponse {

  @Schema(
      description = "Internal payment ID in our database",
      example = "1",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long paymentId;

  @Schema(
      description = "Order code from the original request",
      example = "123456789",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long orderCode;

  @Schema(
      description = "Payment amount in VND",
      example = "50000",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long amount;

  @Schema(
      description = "Payment description",
      example = "Payment for Order #12345",
      accessMode = Schema.AccessMode.READ_ONLY)
  private String description;

  @Schema(
      description = "payOS checkout URL - redirect user to this URL for payment",
      example = "https://pay.payos.vn/web/abc123xyz456",
      accessMode = Schema.AccessMode.READ_ONLY)
  private String checkoutUrl;

  @Schema(
      description = "VietQR QR Code string for payment",
      example = "00020101021126370014A000000727012301234567",
      accessMode = Schema.AccessMode.READ_ONLY)
  private String qrCode;

  @Schema(
      description = "payOS payment link ID for tracking",
      example = "abc123xyz456",
      accessMode = Schema.AccessMode.READ_ONLY)
  private String paymentLinkId;

  @Schema(
      description = "Current payment status",
      example = "PENDING",
      accessMode = Schema.AccessMode.READ_ONLY,
      allowableValues = {"PENDING", "PAID", "CANCELLED", "FAILED"})
  private PaymentStatus status;

  @Schema(
      description = "Payment link expiration time (Unix timestamp in seconds)",
      example = "1731024000",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long expiredAt;

  @Schema(
      description = "Raw payOS response data (all fields returned by payOS)",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Map<String, Object> payosData;
}
