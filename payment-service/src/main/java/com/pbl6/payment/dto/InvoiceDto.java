package com.pbl6.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Invoice information DTO
 * As per payOS docs optional invoice field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Boolean buyerNotGetInvoice;
    private Integer taxPercentage;
}
