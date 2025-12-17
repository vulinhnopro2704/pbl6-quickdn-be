package com.pbl6.order.dto.payment.payos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Webhook payload from payOS
 * As per documentation: https://payos.vn/docs/tich-hop-webhook/
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayosWebhookPayload {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("data")
    private WebhookData data;
    
    @JsonProperty("signature")
    private String signature;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookData {
        @JsonProperty("orderCode")
        private Long orderCode;
        
        @JsonProperty("amount")
        private Long amount;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("accountNumber")
        private String accountNumber;
        
        @JsonProperty("reference")
        private String reference;
        
        @JsonProperty("transactionDateTime")
        private String transactionDateTime;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("paymentLinkId")
        private String paymentLinkId;
        
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("desc")
        private String desc;

        @JsonProperty("counterAccountBankId")
        private String counterAccountBankId;

        @JsonProperty("counterAccountBankName")
        private String counterAccountBankName;

        @JsonProperty("counterAccountName")
        private String counterAccountName;

        @JsonProperty("counterAccountNumber")
        private String counterAccountNumber;

        @JsonProperty("virtualAccountName")
        private String virtualAccountName;

        @JsonProperty("virtualAccountNumber")
        private String virtualAccountNumber;
    }
}
