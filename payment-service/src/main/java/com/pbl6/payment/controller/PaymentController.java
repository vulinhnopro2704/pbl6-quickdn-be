package com.pbl6.payment.controller;

import com.pbl6.payment.config.PayosConfig;
import com.pbl6.payment.dto.CreatePaymentRequest;
import com.pbl6.payment.dto.PaymentResponse;
import com.pbl6.payment.dto.payos.PayosWebhookPayload;
import com.pbl6.payment.entity.Payment;
import com.pbl6.payment.entity.PaymentStatus;
import com.pbl6.payment.exception.PaymentNotFoundException;
import com.pbl6.payment.repository.PaymentRepository;
import com.pbl6.payment.service.PaymentService;
import com.pbl6.payment.util.SignatureHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment controller - REST API endpoints
 * 
 * Endpoints:
 * - POST /api/payment - Create payment link
 * - GET /api/payment/{orderCode} - Get payment by orderCode
 * - POST /api/payment/webhook - Receive payment webhook from payOS
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Payment",
    description = "Payment management APIs - create payment links, retrieve status, and handle webhooks"
)
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PayosConfig payosConfig;

    // ObjectMapper for logging payloads as JSON
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Create payment link
     */
    @Operation(
        summary = "Create a new payment link",
        description = """
            Creates a new payment link via payOS gateway. Returns a checkout URL where users can complete payment.
            
            **Idempotency:** Requests with the same orderCode are idempotent. If a payment with the same orderCode 
            already exists and is PENDING or PAID, the existing payment is returned without calling payOS again.
            
            **Workflow:**
            1. Submit payment request with unique orderCode
            2. System creates payment record and calls payOS API
            3. Receive checkout URL in response
            4. Redirect user to checkout URL
            5. User completes payment on payOS
            6. System receives webhook notification
            7. Payment status updated to PAID
            
            **Required Fields:** orderCode, amount, description, cancelUrl, returnUrl
            
            **Optional Fields:** expiredAt, buyer, items, invoice
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payment creation request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreatePaymentRequest.class),
                examples = @ExampleObject(
                    name = "Basic Payment",
                    summary = "Minimal payment request",
                    value = """
                        {
                          "orderCode": 123456789,
                          "amount": 50000,
                          "description": "Payment for Order #12345",
                          "cancelUrl": "https://myapp.com/payment/cancel?orderId=12345",
                          "returnUrl": "https://myapp.com/payment/success?orderId=12345"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Payment created successfully - redirect user to checkoutUrl",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "paymentId": 1,
                          "orderCode": 123456789,
                          "amount": 50000,
                          "description": "Payment for Order #12345",
                          "checkoutUrl": "https://pay.payos.vn/web/abc123xyz456",
                          "paymentLinkId": "abc123xyz456",
                          "status": "PENDING",
                          "expiredAt": 1731024000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Payment already exists (idempotency) - returning existing payment",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing required fields or validation errors",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2024-11-07T10:30:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "orderCode is required"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - payOS API failure or database error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2024-11-07T10:30:00Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "Failed to create payment: Connection timeout"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Received create payment request: orderCode={}, amount={}", 
            request.getOrderCode(), request.getAmount());
        
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello World!");
    }

    /**
     * Get payment by orderCode
     */
    @Operation(
        summary = "Retrieve payment by order code",
        description = """
            Retrieves payment information by orderCode. Returns payment details including current status.
            
            **Use Cases:**
            - Check payment status after user returns from payOS
            - Verify payment completion before fulfilling order
            - Display payment details in order history
            
            **Payment Status:**
            - PENDING: Payment link created, waiting for user payment
            - PAID: Payment completed successfully
            - CANCELLED: Payment cancelled by user or expired
            - FAILED: Payment failed due to error
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Payment found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "paymentId": 1,
                          "orderCode": 123456789,
                          "amount": 50000,
                          "description": "Payment for Order #12345",
                          "checkoutUrl": "https://pay.payos.vn/web/abc123xyz456",
                          "paymentLinkId": "abc123xyz456",
                          "status": "PAID",
                          "expiredAt": 1731024000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found - no payment exists with the given orderCode",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2024-11-07T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Payment not found"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/{orderCode}")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(
                description = "Unique order code of the payment",
                example = "123456789",
                required = true
            )
            @PathVariable Long orderCode) {
        
        log.info("Getting payment: orderCode={}", orderCode);
        
        Payment payment = paymentService.getPaymentByOrderCode(orderCode)
            .orElseThrow(() -> new PaymentNotFoundException(orderCode));
        
        return ResponseEntity.ok(buildPaymentResponse(payment));
    }
    
    /**
     * Webhook endpoint to receive payment notifications from payOS
     */
    @Operation(
        summary = "Receive webhook notifications from payOS",
        description = """
            **⚠️ This endpoint is called by payOS, not by your frontend!**
            
            Receives payment status updates from payOS when payment is completed, cancelled, or failed.
            
            **Webhook Flow:**
            1. User completes payment on payOS
            2. payOS sends webhook to this endpoint with payment data and signature
            3. System verifies signature using HMAC_SHA256
            4. Updates payment status in database
            5. Returns 200 OK to acknowledge receipt
            
            **Signature Verification:**
            - Build data string from webhook payload (alphabetically sorted)
            - Compute HMAC_SHA256(dataString, CHECKSUM_KEY)
            - Compare with signature in webhook payload
            - Reject if signatures don't match
            
            **Implementation Status:** ⚠️ Currently a stub - signature verification and status update logic need to be completed.
            See comments in code for implementation details.
            
            **Configuration:**
            Set webhook URL in payOS dashboard: `https://your-domain.com/api/payment/webhook`
            """,
        tags = {"Webhook"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Webhook processed successfully",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "OK")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid signature - webhook rejected",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Invalid signature")
            )
        )
    })
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Webhook payload from payOS with payment status and signature",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PayosWebhookPayload.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "code": "00",
                              "desc": "success",
                              "success": true,
                              "data": {
                                "orderCode": 123456789,
                                "amount": 50000,
                                "description": "Payment for Order #12345",
                                "accountNumber": "12345678",
                                "reference": "TF230204212323",
                                "transactionDateTime": "2023-02-04 18:25:00",
                                "currency": "VND",
                                "paymentLinkId": "abc123xyz456",
                                "code": "00",
                                "desc": "Thành công"
                              },
                              "signature": "8d8640d802576397a1ce45ebda7f835055768ac7ad2e0bfb77f9b8f12cca4c7f"
                            }
                            """
                    )
                )
            )
            @RequestBody(required = false) PayosWebhookPayload payload) {

        // Defensive null checks to avoid NPEs from malformed or partial payloads
        if (payload == null) {
            log.warn("Received empty webhook payload");
            return ResponseEntity.badRequest().body("Missing request body");
        }

        if (payload.getData() == null) {
            log.warn("Webhook payload missing 'data' field: {}", payload);
            return ResponseEntity.badRequest().body("Missing 'data' in payload");
        }

        // Log full payload as JSON (safe serialization)
        try {
            String payloadJson = OBJECT_MAPPER.writeValueAsString(payload);
            log.info("Webhook payload JSON: {}", payloadJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize webhook payload to JSON, falling back to toString()", e);
            log.info("Webhook payload (toString): {}", payload);
        }

        // Extract safely for logging to avoid chained NPE
        Long orderCode = payload.getData().getOrderCode();
        String paymentLinkId = payload.getData().getPaymentLinkId();
        String code = payload.getCode();
        Long amount = payload.getData().getAmount();
        String transactionDateTime = payload.getData().getTransactionDateTime();

        log.info("Received webhook: orderCode={}, paymentLinkId={}, code={}, amount={}, transactionDateTime={}",
            orderCode, paymentLinkId, code, amount, transactionDateTime);

        // Basic validation: signature must be present
        if (payload.getSignature() == null || payload.getSignature().isBlank()) {
            log.warn("Webhook payload missing signature: orderCode={}, paymentLinkId={}", orderCode, paymentLinkId);
            return ResponseEntity.badRequest().body("Missing signature");
        }

        // Verify webhook signature
        String dataString = buildWebhookDataString(payload.getData());
        boolean isValid = SignatureHelper.verifyWebhookSignature(
            dataString, 
            payload.getSignature(), 
            payosConfig.getChecksumKey()
        );
        
        if (!isValid) {
            log.error("Invalid webhook signature: orderCode={}, paymentLinkId={}, expected data: {}", 
                orderCode, paymentLinkId, dataString);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        log.info("Webhook signature verified successfully: orderCode={}", orderCode);
        
        // Find payment by paymentLinkId and update status
        java.util.Optional<Payment> paymentOpt = paymentService.getPaymentByPaymentLinkId(
            payload.getData().getPaymentLinkId()
        );
        
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for paymentLinkId={}, orderCode={}", paymentLinkId, orderCode);
            // Still return OK to acknowledge webhook receipt
            return ResponseEntity.ok("OK");
        }
        
        Payment payment = paymentOpt.get();
        PaymentStatus oldStatus = payment.getStatus();
        
        // Update payment status based on webhook code
        if ("00".equals(payload.getCode())) {
            payment.setStatus(PaymentStatus.PAID);
            log.info("Payment status updated to PAID: orderCode={}, paymentLinkId={}", 
                orderCode, paymentLinkId);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment status updated to FAILED: orderCode={}, paymentLinkId={}, code={}, desc={}", 
                orderCode, paymentLinkId, payload.getCode(), payload.getDesc());
        }
        
        // Save updated payment
        paymentRepository.save(payment);
        
        // Save webhook event for audit trail
        savePaymentEvent(payment.getId(), payment.getOrderCode(), "WEBHOOK", payload);
        
        log.info("Webhook processed successfully: orderCode={}, oldStatus={}, newStatus={}", 
            orderCode, oldStatus, payment.getStatus());
        return ResponseEntity.ok("OK");
    }
    
    /**
     * Helper to build payment response from entity
     */
    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
            .paymentId(payment.getId())
            .orderCode(payment.getOrderCode())
            .amount(payment.getAmount())
            .description(payment.getDescription())
            .checkoutUrl(payment.getCheckoutUrl())
            .paymentLinkId(payment.getPayosPaymentLinkId())
            .status(payment.getStatus())
            .expiredAt(payment.getExpiredAt())
            .build();
    }
    
    /**
     * Build webhook data string for signature verification
     * Data format (sorted alphabetically):
     * amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
     * 
     * Note: PayOS webhook doesn't include cancelUrl and returnUrl in the data,
     * so we only use the fields present in the webhook payload:
     * amount=$amount&description=$description&orderCode=$orderCode
     */
    private String buildWebhookDataString(PayosWebhookPayload.WebhookData data) {
        return String.format(
            "amount=%d&description=%s&orderCode=%d",
            data.getAmount(),
            data.getDescription(),
            data.getOrderCode()
        );
    }
    
    /**
     * Save payment event for audit trail
     */
    private void savePaymentEvent(Long paymentId, Long orderCode, String eventType, Object payload) {
        paymentService.savePaymentEventPublic(paymentId, orderCode, eventType, payload);
    }
}
