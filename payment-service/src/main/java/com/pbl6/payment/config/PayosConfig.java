package com.pbl6.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PayOS configuration properties
 * All values are read from environment variables for security
 * 
 * Required ENV vars:
 * - PAYOS_CLIENT_ID
 * - PAYOS_API_KEY
 * - PAYOS_CHECKSUM_KEY
 * 
 * Optional ENV vars:
 * - PAYOS_API_BASE_URL (default: https://api-merchant.payos.vn)
 * - PAYOS_PARTNER_CODE
 * - PAYOS_RETURN_URL
 * - PAYOS_CANCEL_URL
 */
@Configuration
@ConfigurationProperties(prefix = "payos.api")
@Data
public class PayosConfig {
    
    /**
     * PayOS API base URL
     * ENV: PAYOS_API_BASE_URL
     */
    private String baseUrl = "https://api-merchant.payos.vn";
    
    /**
     * Client ID for x-client-id header
     * ENV: PAYOS_CLIENT_ID (REQUIRED)
     */
    private String clientId;
    
    /**
     * API Key for x-api-key header
     * ENV: PAYOS_API_KEY (REQUIRED)
     */
    private String apiKey;
    
    /**
     * Partner code for x-partner-code header (optional)
     * ENV: PAYOS_PARTNER_CODE
     */
    private String partnerCode;
    
    /**
     * Checksum key for signature generation
     * ENV: PAYOS_CHECKSUM_KEY (REQUIRED)
     */
    private String checksumKey;
    
    /**
     * Default return URL when payment succeeds
     * ENV: PAYOS_RETURN_URL
     */
    private String defaultReturnUrl = "http://localhost:8080/payment/result";
    
    /**
     * Default cancel URL when user cancels payment
     * ENV: PAYOS_CANCEL_URL
     */
    private String defaultCancelUrl = "http://localhost:8080/payment/cancel";
    
    /**
     * HTTP connection timeout in milliseconds
     */
    private int connectionTimeout = 10000;
    
    /**
     * HTTP read timeout in milliseconds
     */
    private int readTimeout = 30000;
    
    /**
     * Maximum retry attempts for 5xx errors
     */
    private int maxRetries = 2;
}
