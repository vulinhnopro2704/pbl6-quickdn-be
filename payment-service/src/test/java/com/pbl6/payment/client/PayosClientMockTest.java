package com.pbl6.payment.client;

import com.pbl6.payment.dto.payos.PayosCreatePaymentRequest;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PayosClientMock
 */
class PayosClientMockTest {
    
    private PayosClientMock payosClientMock;
    
    @BeforeEach
    void setUp() {
        payosClientMock = new PayosClientMock();
    }
    
    /**
     * Test creating payment with mock client
     */
    @Test
    void testCreatePayment() {
        // Given: payment request
        PayosCreatePaymentRequest request = PayosCreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .signature("mock_signature")
            .build();
        
        // When: create payment
        PayosCreatePaymentResponse response = payosClientMock.createPayment(request);
        
        // Then: verify response
        assertNotNull(response);
        assertEquals("00", response.getCode());
        assertEquals("success", response.getDesc());
        assertNotNull(response.getData());
        assertEquals(123456L, response.getData().getOrderCode());
        assertEquals(50000L, response.getData().getAmount());
        assertEquals("Test payment", response.getData().getDescription());
        assertTrue(response.getData().getPaymentLinkId().startsWith("mock_"));
        assertTrue(response.getData().getCheckoutUrl().contains("pay.payos.vn"));
        assertEquals("PENDING", response.getData().getStatus());
    }
    
    /**
     * Test getting payment by orderCode
     */
    @Test
    void testGetPaymentByOrderCode() {
        // Given: created payment
        PayosCreatePaymentRequest createRequest = PayosCreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .build();
        
        PayosCreatePaymentResponse createResponse = payosClientMock.createPayment(createRequest);
        String paymentLinkId = createResponse.getData().getPaymentLinkId();
        
        // When: get payment by orderCode
        var response = payosClientMock.getPayment("123456");
        
        // Then: verify response
        assertNotNull(response);
        assertEquals("00", response.getCode());
        assertEquals(123456L, response.getData().getOrderCode());
        assertEquals(50000L, response.getData().getAmount());
    }
    
    /**
     * Test getting payment by paymentLinkId
     */
    @Test
    void testGetPaymentByPaymentLinkId() {
        // Given: created payment
        PayosCreatePaymentRequest createRequest = PayosCreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .build();
        
        PayosCreatePaymentResponse createResponse = payosClientMock.createPayment(createRequest);
        String paymentLinkId = createResponse.getData().getPaymentLinkId();
        
        // When: get payment by paymentLinkId
        var response = payosClientMock.getPayment(paymentLinkId);
        
        // Then: verify response
        assertNotNull(response);
        assertEquals(paymentLinkId, response.getData().getId());
    }
    
    /**
     * Test cancelling payment
     */
    @Test
    void testCancelPayment() {
        // Given: created payment
        PayosCreatePaymentRequest createRequest = PayosCreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .build();
        
        PayosCreatePaymentResponse createResponse = payosClientMock.createPayment(createRequest);
        String paymentLinkId = createResponse.getData().getPaymentLinkId();
        
        // When: cancel payment
        var response = payosClientMock.cancelPayment(paymentLinkId, "User cancelled");
        
        // Then: verify response
        assertNotNull(response);
        assertEquals("00", response.getCode());
        assertEquals("CANCELLED", response.getData().getStatus());
        assertEquals("User cancelled", response.getData().getCancellationReason());
    }
    
    /**
     * Test simulate payment success
     */
    @Test
    void testSimulatePaymentSuccess() {
        // Given: created payment
        PayosCreatePaymentRequest createRequest = PayosCreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .build();
        
        PayosCreatePaymentResponse createResponse = payosClientMock.createPayment(createRequest);
        String paymentLinkId = createResponse.getData().getPaymentLinkId();
        
        // When: simulate payment success
        payosClientMock.simulatePaymentSuccess(paymentLinkId);
        
        // Then: verify status changed to PAID
        var response = payosClientMock.getPayment(paymentLinkId);
        assertEquals("PAID", response.getData().getStatus());
    }
    
    /**
     * Test getting non-existent payment throws exception
     */
    @Test
    void testGetNonExistentPayment_ThrowsException() {
        // When/Then: getting non-existent payment should throw exception
        assertThrows(RuntimeException.class, () -> {
            payosClientMock.getPayment("non_existent_id");
        });
    }
}
