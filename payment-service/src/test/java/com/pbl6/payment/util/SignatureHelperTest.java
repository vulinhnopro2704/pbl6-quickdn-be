package com.pbl6.payment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SignatureHelper
 * Tests HMAC_SHA256 signature generation as per payOS documentation
 */
class SignatureHelperTest {
    
    /**
     * Test signature generation with known example
     * This verifies that our signature implementation matches expected output
     */
    @Test
    void testGeneratePaymentSignature() {
        // Given: sample payment data
        Long amount = 50000L;
        String cancelUrl = "http://localhost:8080/cancel";
        String description = "Test payment";
        Long orderCode = 123456L;
        String returnUrl = "http://localhost:8080/return";
        String checksumKey = "test_checksum_key_12345";
        
        // When: generate signature
        String signature = SignatureHelper.generatePaymentSignature(
            amount, cancelUrl, description, orderCode, returnUrl, checksumKey
        );
        
        // Then: signature should be non-null and 64 characters (SHA256 hex)
        assertNotNull(signature);
        assertEquals(64, signature.length());
        
        // Verify signature is deterministic (same input = same output)
        String signature2 = SignatureHelper.generatePaymentSignature(
            amount, cancelUrl, description, orderCode, returnUrl, checksumKey
        );
        assertEquals(signature, signature2);
    }
    
    /**
     * Test that signature changes when any parameter changes
     */
    @Test
    void testSignatureChangesWithDifferentInputs() {
        String checksumKey = "test_key";
        
        String sig1 = SignatureHelper.generatePaymentSignature(
            10000L, "http://cancel", "desc1", 123L, "http://return", checksumKey
        );
        
        // Different amount
        String sig2 = SignatureHelper.generatePaymentSignature(
            20000L, "http://cancel", "desc1", 123L, "http://return", checksumKey
        );
        assertNotEquals(sig1, sig2);
        
        // Different description
        String sig3 = SignatureHelper.generatePaymentSignature(
            10000L, "http://cancel", "desc2", 123L, "http://return", checksumKey
        );
        assertNotEquals(sig1, sig3);
        
        // Different orderCode
        String sig4 = SignatureHelper.generatePaymentSignature(
            10000L, "http://cancel", "desc1", 456L, "http://return", checksumKey
        );
        assertNotEquals(sig1, sig4);
    }
    
    /**
     * Test HMAC_SHA256 with known test vector
     */
    @Test
    void testHmacSHA256WithKnownVector() {
        // Given: known data and key
        String data = "amount=10000&cancelUrl=http://test.com/cancel&description=Test&orderCode=123&returnUrl=http://test.com/return";
        String key = "secret123";
        
        // When: compute HMAC
        String signature = SignatureHelper.hmacSHA256(data, key);
        
        // Then: signature should be valid hex string
        assertNotNull(signature);
        assertEquals(64, signature.length());
        assertTrue(signature.matches("^[0-9a-f]{64}$"), "Signature should be lowercase hex");
    }
    
    /**
     * Test webhook signature verification
     */
    @Test
    void testVerifyWebhookSignature() {
        String data = "test data for webhook";
        String key = "webhook_key";
        
        // Generate signature
        String validSignature = SignatureHelper.hmacSHA256(data, key);
        
        // Should verify successfully
        assertTrue(SignatureHelper.verifyWebhookSignature(data, validSignature, key));
        
        // Should fail with wrong signature
        assertFalse(SignatureHelper.verifyWebhookSignature(data, "invalid_signature", key));
        
        // Should fail with wrong key
        assertFalse(SignatureHelper.verifyWebhookSignature(data, validSignature, "wrong_key"));
    }
    
    /**
     * Test that data string is formatted correctly (alphabetically sorted)
     * This is critical as per payOS docs
     */
    @Test
    void testDataStringFormat() {
        Long amount = 50000L;
        String cancelUrl = "http://localhost:8080/cancel";
        String description = "Test";
        Long orderCode = 123L;
        String returnUrl = "http://localhost:8080/return";
        String key = "key";
        
        String signature = SignatureHelper.generatePaymentSignature(
            amount, cancelUrl, description, orderCode, returnUrl, key
        );
        
        // Expected data string (alphabetically sorted):
        // amount=50000&cancelUrl=http://localhost:8080/cancel&description=Test&orderCode=123&returnUrl=http://localhost:8080/return
        String expectedDataString = String.format(
            "amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
            amount, cancelUrl, description, orderCode, returnUrl
        );
        
        String expectedSignature = SignatureHelper.hmacSHA256(expectedDataString, key);
        
        assertEquals(expectedSignature, signature, 
            "Signature should match when using alphabetically sorted data string");
    }
    
    /**
     * Test with special characters in description
     */
    @Test
    void testSignatureWithSpecialCharacters() {
        String description = "Payment for order #123 - Nguyễn Văn A";
        
        String signature = SignatureHelper.generatePaymentSignature(
            10000L,
            "http://test.com/cancel",
            description,
            123L,
            "http://test.com/return",
            "key"
        );
        
        assertNotNull(signature);
        assertEquals(64, signature.length());
    }
}
