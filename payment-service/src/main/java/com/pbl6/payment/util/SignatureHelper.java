package com.pbl6.payment.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Signature helper for payOS API
 * Implements HMAC_SHA256 signature generation as per payOS documentation
 * 
 * Data format (sorted alphabetically):
 * amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
 */
public class SignatureHelper {
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    /**
     * Generate signature for payment request
     * As per payOS docs: sort keys alphabetically and format as key=value&key=value
     * 
     * @param amount Payment amount
     * @param cancelUrl Cancel URL
     * @param description Payment description
     * @param orderCode Order code
     * @param returnUrl Return URL
     * @param checksumKey Checksum key from payOS channel
     * @return HMAC_SHA256 hex signature
     */
    public static String generatePaymentSignature(
            Long amount,
            String cancelUrl,
            String description,
            Long orderCode,
            String returnUrl,
            String checksumKey) {
        
        // Build data string (alphabetically sorted keys)
        String dataString = String.format(
            "amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
            amount, cancelUrl, description, orderCode, returnUrl
        );
        
        return hmacSHA256(dataString, checksumKey);
    }
    
    /**
     * Compute HMAC_SHA256 signature
     * 
     * @param data Data to sign
     * @param key Secret key
     * @return Hex-encoded signature
     */
    public static String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
            );
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string (lowercase)
            return HexFormat.of().formatHex(hash);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC_SHA256 signature", e);
        }
    }
    
    /**
     * Verify webhook signature
     * Compare computed signature with provided signature
     * 
     * @param data Data to verify
     * @param providedSignature Signature from payOS webhook
     * @param checksumKey Checksum key
     * @return true if signature is valid
     */
    public static boolean verifyWebhookSignature(
            String data,
            String providedSignature,
            String checksumKey) {
        
        String computedSignature = hmacSHA256(data, checksumKey);
        return computedSignature.equalsIgnoreCase(providedSignature);
    }
}
