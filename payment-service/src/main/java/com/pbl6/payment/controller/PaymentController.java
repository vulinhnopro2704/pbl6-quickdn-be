package com.pbl6.payment.controller;

import com.pbl6.payment.dto.CreatePaymentRequest;
import com.pbl6.payment.dto.PaymentResponse;
import com.pbl6.payment.dto.payos.PayosWebhookPayload;
import com.pbl6.payment.entity.Payment;
import com.pbl6.payment.exception.PaymentNotFoundException;
import com.pbl6.payment.service.PaymentService;
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
 * - POST /api/payments - Create payment link
 * - GET /api/payments/{orderCode} - Get payment by orderCode
 * - POST /api/payments/webhook - Receive payment webhook from payOS
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
            Set webhook URL in payOS dashboard: `https://your-domain.com/api/payments/webhook`
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
            @RequestBody PayosWebhookPayload payload) {
        
        log.info("Received webhook: orderCode={}, paymentLinkId={}, code={}", 
            payload.getData().getOrderCode(),
            payload.getData().getPaymentLinkId(),
            payload.getCode());
        
        // TODO: Implement webhook signature verification
        // String dataString = buildWebhookDataString(payload.getData());
        // boolean isValid = SignatureHelper.verifyWebhookSignature(
        //     dataString, 
        //     payload.getSignature(), 
        //     payosConfig.getChecksumKey()
        // );
        // 
        // if (!isValid) {
        //     log.error("Invalid webhook signature");
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        // }
        
        // TODO: Find payment by paymentLinkId and update status
        // Optional<Payment> payment = paymentService.getPaymentByPaymentLinkId(
        //     payload.getData().getPaymentLinkId()
        // );
        // 
        // if (payment.isPresent()) {
        //     Payment p = payment.get();
        //     
        //     if ("00".equals(payload.getCode())) {
        //         p.setStatus(PaymentStatus.PAID);
        //     } else {
        //         p.setStatus(PaymentStatus.FAILED);
        //     }
        //     
        //     paymentRepository.save(p);
        //     
        //     // Save webhook event
        //     savePaymentEvent(p.getId(), p.getOrderCode(), "WEBHOOK", payload);
        // }
        
        log.info("Webhook processed (stub implementation)");
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
}