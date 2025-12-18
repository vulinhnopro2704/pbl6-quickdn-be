package com.pbl6.order.dto.payment.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from payOS API: POST /v2/payment-requests
 * Wraps the data in standard payOS response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosCreatePaymentResponse {
    
    @JsonProperty("code")
    private String code; // "00" = success
    
    @JsonProperty("desc")
    private String desc; // "success"
    
    @JsonProperty("data")
    private PaymentData data;
    
    @JsonProperty("signature")
    private String signature;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        @JsonProperty("bin")
        private String bin;
        
        @JsonProperty("accountNumber")
        private String accountNumber;
        
        @JsonProperty("accountName")
        private String accountName;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("paymentLinkId")
        private String paymentLinkId;
        
        @JsonProperty("amount")
        private Long amount;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("orderCode")
        private Long orderCode;
        
        @JsonProperty("expiredAt")
        private Long expiredAt;
        
        @JsonProperty("status")
        private String status; // "PAID", "PENDING", "CANCELLED"
        
        @JsonProperty("checkoutUrl")
        private String checkoutUrl;
        
        @JsonProperty("qrCode")
        private String qrCode;
    }
}
