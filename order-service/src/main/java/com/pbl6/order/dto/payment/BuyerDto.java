package com.pbl6.order.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Buyer information DTO Used for creating electronic invoices as per payOS docs */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BuyerDto", description = "Buyer/customer information for invoice generation")
public class BuyerDto {

  @Schema(description = "Full name of the buyer", example = "Nguyễn Văn A")
  private String name;

  @Schema(description = "Company name (for business buyers)", example = "Công ty TNHH ABC")
  private String companyName;

  @Schema(description = "Tax code (for business buyers requiring invoice)", example = "0123456789")
  private String taxCode;

  @Schema(description = "Address of the buyer", example = "123 Đường ABC, Quận 1, TP.HCM")
  private String address;

  @Schema(description = "Email address for sending invoice", example = "nguyenvana@example.com")
  private String email;

  @Schema(description = "Phone number", example = "0912345678")
  private String phone;
}
