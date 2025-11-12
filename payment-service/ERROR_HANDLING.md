# Error Handling Documentation

## 📋 Overview

Payment Service sử dụng **Global Exception Handler** để xử lý tất cả các lỗi một cách nhất quán, trả về response format chuẩn theo OpenAPI specification.

## 🎯 Error Response Format

Tất cả lỗi đều trả về theo format sau:

```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "orderCode is required",
  "path": "/api/payments"
}
```

## 🔴 HTTP Status Codes

| Status | Error Type | Description |
|--------|-----------|-------------|
| **400** | Bad Request | Validation errors, missing required fields |
| **401** | Unauthorized | Invalid webhook signature |
| **404** | Not Found | Payment not found |
| **500** | Internal Server Error | Payment creation failed, PayOS API errors, unexpected errors |

## 🛠️ Custom Exceptions

### 1. `PaymentNotFoundException` (404)

**Khi nào throw:**
- Không tìm thấy payment với orderCode đã cho

**Ví dụ:**
```java
throw new PaymentNotFoundException(orderCode);
// hoặc
throw new PaymentNotFoundException("Payment not found with orderCode: " + orderCode);
```

**Response:**
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Payment not found with orderCode: 123456789",
  "path": "/api/payments/123456789"
}
```

---

### 2. `PaymentCreationException` (500)

**Khi nào throw:**
- Lỗi khi tạo payment (database, PayOS API, business logic)
- Bất kỳ lỗi nào trong quá trình xử lý payment creation

**Ví dụ:**
```java
throw new PaymentCreationException("Failed to create payment: Connection timeout");
// hoặc với cause
throw new PaymentCreationException("Failed to create payment", originalException);
```

**Response:**
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to create payment: Connection timeout",
  "path": "/api/payments"
}
```

---

### 3. `PayosApiException` (500)

**Khi nào throw:**
- PayOS API trả về lỗi
- Timeout khi gọi PayOS API
- Network errors

**Ví dụ:**
```java
throw new PayosApiException("PayOS API returned error: Invalid signature");
// hoặc
throw new PayosApiException("PayOS API timeout", timeoutException);
```

**Response:**
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to create payment: PayOS API returned error: Invalid signature",
  "path": "/api/payments"
}
```

---

### 4. `InvalidWebhookSignatureException` (401)

**Khi nào throw:**
- Webhook signature verification failed
- Signature không khớp với expected value

**Ví dụ:**
```java
throw new InvalidWebhookSignatureException("Invalid signature");
```

**Response:**
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid signature",
  "path": "/api/payments/webhook"
}
```

---

### 5. `MethodArgumentNotValidException` (400)

**Khi nào throw:**
- Tự động throw bởi Spring khi validation fail
- Sử dụng `@Valid` annotation trên request body

**Validation annotations:**
- `@NotNull` - field không được null
- `@NotBlank` - string không được empty hoặc blank
- `@Min` - số phải >= giá trị min
- `@Valid` - validate nested objects

**Ví dụ validation errors:**

Single field error:
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "orderCode is required",
  "path": "/api/payments"
}
```

Multiple field errors:
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "orderCode: orderCode is required, amount: amount must be at least 1000 VND",
  "path": "/api/payments"
}
```

---

## 📝 Usage Examples

### Controller Level

```java
@GetMapping("/{orderCode}")
public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long orderCode) {
    // Không cần try-catch! GlobalExceptionHandler sẽ xử lý
    Payment payment = paymentService.getPaymentByOrderCode(orderCode)
        .orElseThrow(() -> new PaymentNotFoundException(orderCode));
    
    return ResponseEntity.ok(buildPaymentResponse(payment));
}
```

### Service Level

```java
@Transactional
public PaymentResponse createPayment(CreatePaymentRequest request) {
    try {
        // Business logic
        PayosCreatePaymentResponse response = payosClient.createPayment(payosRequest);
        // Save to database
        return buildPaymentResponse(payment);
        
    } catch (PayosApiException e) {
        // Mark payment as FAILED
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        
        // Throw custom exception - GlobalExceptionHandler sẽ xử lý
        throw new PaymentCreationException("Failed to create payment: " + e.getMessage(), e);
        
    } catch (Exception e) {
        // Unexpected errors
        throw new PaymentCreationException("Failed to create payment: " + e.getMessage(), e);
    }
}
```

### Webhook Signature Verification

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(@RequestBody PayosWebhookPayload payload) {
    // Verify signature
    boolean isValid = SignatureHelper.verifyWebhookSignature(
        dataString, 
        payload.getSignature(), 
        payosConfig.getChecksumKey()
    );
    
    if (!isValid) {
        throw new InvalidWebhookSignatureException("Invalid signature");
    }
    
    // Process webhook...
    return ResponseEntity.ok("OK");
}
```

---

## ✅ Best Practices

### 1. **Không dùng generic Exception trong controller**
❌ Bad:
```java
@GetMapping("/{orderCode}")
public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long orderCode) {
    try {
        Payment payment = paymentService.getPaymentByOrderCode(orderCode)
            .orElseThrow(() -> new RuntimeException("Not found"));
        return ResponseEntity.ok(buildPaymentResponse(payment));
    } catch (Exception e) {
        return ResponseEntity.status(500).build();
    }
}
```

✅ Good:
```java
@GetMapping("/{orderCode}")
public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long orderCode) {
    Payment payment = paymentService.getPaymentByOrderCode(orderCode)
        .orElseThrow(() -> new PaymentNotFoundException(orderCode));
    return ResponseEntity.ok(buildPaymentResponse(payment));
}
```

### 2. **Throw specific exceptions trong service**
❌ Bad:
```java
throw new RuntimeException("Something went wrong");
```

✅ Good:
```java
throw new PaymentCreationException("Failed to create payment: " + details);
```

### 3. **Sử dụng validation annotations**
❌ Bad:
```java
@PostMapping
public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
    if (request.getOrderCode() == null) {
        throw new IllegalArgumentException("orderCode is required");
    }
    // ...
}
```

✅ Good:
```java
@PostMapping
public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
    // Validation tự động, GlobalExceptionHandler xử lý lỗi
    // ...
}
```

### 4. **Log errors trước khi throw**
```java
try {
    // Business logic
} catch (PayosApiException e) {
    log.error("PayOS API failed: orderCode={}", orderCode, e);
    throw new PaymentCreationException("Failed to create payment", e);
}
```

---

## 🔍 Testing Error Responses

### Test 400 - Validation Error
```bash
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{}'
```

Expected:
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "orderCode is required",
  "path": "/api/payments"
}
```

### Test 404 - Not Found
```bash
curl -X GET http://localhost:8084/api/payments/999999999
```

Expected:
```json
{
  "timestamp": "2024-11-07T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Payment not found with orderCode: 999999999",
  "path": "/api/payments/999999999"
}
```

### Test 401 - Invalid Webhook Signature
```bash
curl -X POST http://localhost:8084/api/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "code": "00",
    "signature": "invalid_signature",
    "data": {}
  }'
```

---

## 📊 Exception Flow

```
Client Request
     ↓
Controller (@Valid validation)
     ↓
     ├─ Validation Failed? → MethodArgumentNotValidException → GlobalExceptionHandler → 400
     └─ Validation OK
          ↓
     Service Layer
          ↓
          ├─ PaymentNotFoundException → GlobalExceptionHandler → 404
          ├─ PaymentCreationException → GlobalExceptionHandler → 500
          ├─ PayosApiException → GlobalExceptionHandler → 500
          ├─ InvalidWebhookSignatureException → GlobalExceptionHandler → 401
          └─ Any other Exception → GlobalExceptionHandler → 500
               ↓
          ErrorResponse (JSON)
               ↓
          Client receives consistent error format
```

---
