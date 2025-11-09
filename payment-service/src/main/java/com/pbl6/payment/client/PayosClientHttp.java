package com.pbl6.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.payment.config.PayosConfig;
import com.pbl6.payment.dto.payos.PayosCreatePaymentRequest;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import com.pbl6.payment.dto.payos.PayosGetPaymentResponse;
import com.pbl6.payment.exception.PayosApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP implementation of PayosClient
 * Makes actual HTTP calls to payOS API
 */
@Slf4j
@Component
@Primary
public class PayosClientHttp implements PayosClient {
    
    private final RestTemplate restTemplate;
    private final PayosConfig config;
    private final ObjectMapper objectMapper;
    
    public PayosClientHttp(PayosConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        
        // Build RestTemplate with timeouts
        this.restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
            .setReadTimeout(Duration.ofMillis(config.getReadTimeout()))
            .build();
    }
    
    @Override
    public PayosCreatePaymentResponse createPayment(PayosCreatePaymentRequest request) {
        String url = config.getBaseUrl() + "/v2/payment-requests";
        
        log.info("Creating payment in payOS: orderCode={}, amount={}", 
            request.getOrderCode(), request.getAmount());
        
        HttpHeaders headers = buildHeaders();
        HttpEntity<PayosCreatePaymentRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<PayosCreatePaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                PayosCreatePaymentResponse.class
            );
            
            PayosCreatePaymentResponse responseBody = response.getBody();
            
            if (responseBody == null) {
                throw new PayosApiException("Empty response from payOS");
            }
            
            // Check response code ("00" = success)
            if (!"00".equals(responseBody.getCode())) {
                log.error("payOS API error: code={}, desc={}", 
                    responseBody.getCode(), responseBody.getDesc());
                throw new PayosApiException("payOS error: " + responseBody.getDesc());
            }
            
            log.info("Payment created successfully: paymentLinkId={}", 
                responseBody.getData().getPaymentLinkId());
            
            return responseBody;
            
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error calling payOS API: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayosApiException("payOS API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling payOS API", e);
            throw new PayosApiException("Failed to create payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PayosGetPaymentResponse getPayment(String id) {
        String url = config.getBaseUrl() + "/v2/payment-requests/" + id;
        
        log.info("Getting payment info from payOS: id={}", id);
        
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<PayosGetPaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PayosGetPaymentResponse.class
            );
            
            PayosGetPaymentResponse responseBody = response.getBody();
            
            if (responseBody == null) {
                throw new PayosApiException("Empty response from payOS");
            }
            
            if (!"00".equals(responseBody.getCode())) {
                log.error("payOS API error: code={}, desc={}", 
                    responseBody.getCode(), responseBody.getDesc());
                throw new PayosApiException("payOS error: " + responseBody.getDesc());
            }
            
            return responseBody;
            
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error calling payOS API: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayosApiException("payOS API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling payOS API", e);
            throw new PayosApiException("Failed to get payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PayosGetPaymentResponse cancelPayment(String id, String cancellationReason) {
        String url = config.getBaseUrl() + "/v2/payment-requests/" + id + "/cancel";
        
        log.info("Cancelling payment in payOS: id={}, reason={}", id, cancellationReason);
        
        HttpHeaders headers = buildHeaders();
        Map<String, String> body = Map.of("cancellationReason", 
            cancellationReason != null ? cancellationReason : "Cancelled by user");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<PayosGetPaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                PayosGetPaymentResponse.class
            );
            
            PayosGetPaymentResponse responseBody = response.getBody();
            
            if (responseBody == null) {
                throw new PayosApiException("Empty response from payOS");
            }
            
            if (!"00".equals(responseBody.getCode())) {
                log.error("payOS API error: code={}, desc={}", 
                    responseBody.getCode(), responseBody.getDesc());
                throw new PayosApiException("payOS error: " + responseBody.getDesc());
            }
            
            return responseBody;
            
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error calling payOS API: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PayosApiException("payOS API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling payOS API", e);
            throw new PayosApiException("Failed to cancel payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build HTTP headers for payOS API requests
     * As per docs: x-client-id and x-api-key are required
     * x-partner-code is optional
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Required headers per payOS documentation
        headers.set("x-client-id", config.getClientId());
        headers.set("x-api-key", config.getApiKey());
        
        // Optional partner code
        if (config.getPartnerCode() != null && !config.getPartnerCode().isBlank()) {
            headers.set("x-partner-code", config.getPartnerCode());
        }
        
        return headers;
    }
}
