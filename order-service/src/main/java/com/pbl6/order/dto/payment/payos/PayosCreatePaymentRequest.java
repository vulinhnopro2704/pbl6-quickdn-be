package com.pbl6.order.dto.payment.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.pbl6.order.dto.payment.InvoiceDto;
import com.pbl6.order.dto.payment.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for payOS API: POST /v2/payment-requests
 * Follows exact field names from payOS documentation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosCreatePaymentRequest {
    
    @JsonProperty("orderCode")
    private Long orderCode;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("buyerName")
    private String buyerName;
    
    @JsonProperty("buyerCompanyName")
    private String buyerCompanyName;
    
    @JsonProperty("buyerTaxCode")
    private String buyerTaxCode;
    
    @JsonProperty("buyerAddress")
    private String buyerAddress;
    
    @JsonProperty("buyerEmail")
    private String buyerEmail;
    
    @JsonProperty("buyerPhone")
    private String buyerPhone;
    
    @JsonProperty("items")
    private List<ItemDto> items;
    
    @JsonProperty("cancelUrl")
    private String cancelUrl;
    
    @JsonProperty("returnUrl")
    private String returnUrl;
    
    @JsonProperty("invoice")
    private InvoiceDto invoice;
    
    @JsonProperty("expiredAt")
    private Long expiredAt; // Unix timestamp (Int32)
    
    @JsonProperty("signature")
    private String signature; // HMAC_SHA256 signature
}
