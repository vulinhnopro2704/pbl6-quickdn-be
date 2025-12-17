package com.pbl6.order.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Item in payment request As per payOS API docs: items array in payment request body */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ItemDto", description = "Item/product in the order")
public class ItemDto {

  @Schema(
      description = "Name of the product/service",
      example = "MacBook Pro 16-inch",
      required = true)
  @NotBlank(message = "Item name is required")
  private String name;

  @Schema(description = "Quantity of items", example = "2", required = true, minimum = "1")
  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;

  @Schema(
      description = "Price per unit in VND",
      example = "25000000",
      required = true,
      minimum = "0")
  @NotNull(message = "Price is required")
  @Min(value = 0, message = "Price must be non-negative")
  private Long price;

  @Schema(description = "Unit of measurement", example = "cái", defaultValue = "cái")
  private String unit;

  @Schema(description = "Tax percentage (VAT)", example = "10", minimum = "0", maximum = "100")
  @Min(value = 0, message = "Tax percentage must be non-negative")
  private Integer taxPercentage;
}
