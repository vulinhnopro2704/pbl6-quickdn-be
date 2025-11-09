package com.pbl6.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.payment.client.PayosClient;
import com.pbl6.payment.config.PayosConfig;
import com.pbl6.payment.dto.BuyerDto;
import com.pbl6.payment.dto.CreatePaymentRequest;
import com.pbl6.payment.dto.ItemDto;
import com.pbl6.payment.dto.PaymentResponse;
import com.pbl6.payment.dto.payos.PayosCreatePaymentResponse;
import com.pbl6.payment.entity.Payment;
import com.pbl6.payment.entity.PaymentStatus;
import com.pbl6.payment.repository.PaymentEventRepository;
import com.pbl6.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentEventRepository paymentEventRepository;
    
    @Mock
    private PayosClient payosClient;
    
    @Mock
    private PayosConfig payosConfig;
    
    private PaymentService paymentService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        paymentService = new PaymentService(
            paymentRepository,
            paymentEventRepository,
            payosClient,
            payosConfig,
            objectMapper
        );
        
        // Setup default config mocks
        when(payosConfig.getChecksumKey()).thenReturn("test_checksum_key");
        when(payosConfig.getDefaultCancelUrl()).thenReturn("http://localhost:8080/cancel");
        when(payosConfig.getDefaultReturnUrl()).thenReturn("http://localhost:8080/return");
    }
    
    /**
     * Test successful payment creation
     */
    @Test
    void testCreatePayment_Success() {
        // Given: payment request
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .build();
        
        // Mock: no existing payment
        when(paymentRepository.findByOrderCode(123456L)).thenReturn(Optional.empty());
        
        // Mock: payment save
        Payment savedPayment = Payment.builder()
            .id(1L)
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .status(PaymentStatus.PENDING)
            .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        
        // Mock: payOS response
        PayosCreatePaymentResponse.PaymentData payosData = PayosCreatePaymentResponse.PaymentData.builder()
            .paymentLinkId("mock_payment_link_123")
            .checkoutUrl("https://pay.payos.vn/web/mock_payment_link_123")
            .orderCode(123456L)
            .amount(50000L)
            .status("PENDING")
            .expiredAt(System.currentTimeMillis() / 1000 + 3600)
            .build();
        
        PayosCreatePaymentResponse payosResponse = PayosCreatePaymentResponse.builder()
            .code("00")
            .desc("success")
            .data(payosData)
            .build();
        
        when(payosClient.createPayment(any())).thenReturn(payosResponse);
        
        // When: create payment
        PaymentResponse response = paymentService.createPayment(request);
        
        // Then: verify response
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals(123456L, response.getOrderCode());
        assertEquals(50000L, response.getAmount());
        assertEquals("Test payment", response.getDescription());
        assertEquals("https://pay.payos.vn/web/mock_payment_link_123", response.getCheckoutUrl());
        assertEquals("mock_payment_link_123", response.getPaymentLinkId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        
        // Verify interactions
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(payosClient, times(1)).createPayment(any());
    }
    
    /**
     * Test idempotency: return existing PENDING payment
     */
    @Test
    void testCreatePayment_IdempotencyWithPendingPayment() {
        // Given: payment request
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .build();
        
        // Mock: existing PENDING payment
        Payment existingPayment = Payment.builder()
            .id(1L)
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .status(PaymentStatus.PENDING)
            .payosPaymentLinkId("existing_link_123")
            .checkoutUrl("https://pay.payos.vn/web/existing_link_123")
            .build();
        
        when(paymentRepository.findByOrderCode(123456L)).thenReturn(Optional.of(existingPayment));
        
        // When: create payment
        PaymentResponse response = paymentService.createPayment(request);
        
        // Then: should return existing payment without calling payOS
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals("existing_link_123", response.getPaymentLinkId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        
        // Verify payOS was NOT called
        verify(payosClient, never()).createPayment(any());
        verify(paymentRepository, never()).save(any());
    }
    
    /**
     * Test idempotency: return existing PAID payment
     */
    @Test
    void testCreatePayment_IdempotencyWithPaidPayment() {
        // Given: payment request
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .build();
        
        // Mock: existing PAID payment
        Payment existingPayment = Payment.builder()
            .id(1L)
            .orderCode(123456L)
            .status(PaymentStatus.PAID)
            .payosPaymentLinkId("paid_link_123")
            .checkoutUrl("https://pay.payos.vn/web/paid_link_123")
            .build();
        
        when(paymentRepository.findByOrderCode(123456L)).thenReturn(Optional.of(existingPayment));
        
        // When: create payment
        PaymentResponse response = paymentService.createPayment(request);
        
        // Then: should return existing payment
        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(payosClient, never()).createPayment(any());
    }
    
    /**
     * Test creating new payment when previous was CANCELLED
     */
    @Test
    void testCreatePayment_AllowRecreateAfterCancelled() {
        // Given: payment request
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .build();
        
        // Mock: existing CANCELLED payment
        Payment existingPayment = Payment.builder()
            .id(1L)
            .orderCode(123456L)
            .status(PaymentStatus.CANCELLED)
            .build();
        
        when(paymentRepository.findByOrderCode(123456L))
            .thenReturn(Optional.of(existingPayment));
        
        Payment newPayment = Payment.builder()
            .id(2L)
            .orderCode(123456L)
            .amount(50000L)
            .status(PaymentStatus.PENDING)
            .build();
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(newPayment);
        
        PayosCreatePaymentResponse.PaymentData payosData = PayosCreatePaymentResponse.PaymentData.builder()
            .paymentLinkId("new_link_123")
            .checkoutUrl("https://pay.payos.vn/web/new_link_123")
            .build();
        
        PayosCreatePaymentResponse payosResponse = PayosCreatePaymentResponse.builder()
            .code("00")
            .data(payosData)
            .build();
        
        when(payosClient.createPayment(any())).thenReturn(payosResponse);
        
        // When: create payment
        PaymentResponse response = paymentService.createPayment(request);
        
        // Then: should create new payment
        assertNotNull(response);
        assertEquals(2L, response.getPaymentId());
        verify(payosClient, times(1)).createPayment(any());
    }
    
    /**
     * Test payment creation with buyer and items
     */
    @Test
    void testCreatePayment_WithBuyerAndItems() {
        // Given: payment request with buyer and items
        BuyerDto buyer = BuyerDto.builder()
            .name("Nguyen Van A")
            .email("nguyenvana@test.com")
            .phone("0912345678")
            .build();
        
        List<ItemDto> items = List.of(
            ItemDto.builder()
                .name("Product 1")
                .quantity(2)
                .price(25000L)
                .build()
        );
        
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .orderCode(123456L)
            .amount(50000L)
            .description("Test payment")
            .cancelUrl("http://test.com/cancel")
            .returnUrl("http://test.com/return")
            .buyer(buyer)
            .items(items)
            .build();
        
        when(paymentRepository.findByOrderCode(any())).thenReturn(Optional.empty());
        
        Payment savedPayment = Payment.builder()
            .id(1L)
            .orderCode(123456L)
            .status(PaymentStatus.PENDING)
            .build();
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        
        PayosCreatePaymentResponse payosResponse = PayosCreatePaymentResponse.builder()
            .code("00")
            .data(PayosCreatePaymentResponse.PaymentData.builder()
                .paymentLinkId("link_123")
                .checkoutUrl("https://pay.payos.vn/web/link_123")
                .build())
            .build();
        when(payosClient.createPayment(any())).thenReturn(payosResponse);
        
        // When: create payment
        PaymentResponse response = paymentService.createPayment(request);
        
        // Then: verify payOS was called with buyer and items
        ArgumentCaptor<com.pbl6.payment.dto.payos.PayosCreatePaymentRequest> captor = 
            ArgumentCaptor.forClass(com.pbl6.payment.dto.payos.PayosCreatePaymentRequest.class);
        verify(payosClient).createPayment(captor.capture());
        
        com.pbl6.payment.dto.payos.PayosCreatePaymentRequest payosRequest = captor.getValue();
        assertEquals("Nguyen Van A", payosRequest.getBuyerName());
        assertEquals("nguyenvana@test.com", payosRequest.getBuyerEmail());
        assertEquals(1, payosRequest.getItems().size());
    }
}
