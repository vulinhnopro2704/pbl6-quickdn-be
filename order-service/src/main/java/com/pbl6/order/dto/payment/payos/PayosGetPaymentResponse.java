package com.pbl6.order.dto.payment.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from payOS API: GET /v2/payment-requests/{id}
 * Get payment link information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosGetPaymentResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("data")
    private PaymentInfo data;
    
    @JsonProperty("signature")
    private String signature;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("orderCode")
        private Long orderCode;
        
        @JsonProperty("amount")
        private Long amount;
        
        @JsonProperty("amountPaid")
        private Long amountPaid;
        
        @JsonProperty("amountRemaining")
        private Long amountRemaining;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("createdAt")
        private OffsetDateTime createdAt;
        
        @JsonProperty("transactions")
        private List<Transaction> transactions;
        
        @JsonProperty("canceledAt")
        private OffsetDateTime canceledAt;
        
        @JsonProperty("cancellationReason")
        private String cancellationReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
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
        
        @JsonProperty("virtualAccountName")
        private String virtualAccountName;
        
        @JsonProperty("virtualAccountNumber")
        private String virtualAccountNumber;
    }
}
