# Payment Service - payOS Integration

Full implementation of payOS payment gateway integration for Spring Boot 3.x with PostgreSQL.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Signature Generation](#signature-generation)
- [Webhook Integration](#webhook-integration)
- [Database Schema](#database-schema)
- [Assumptions & Notes](#assumptions--notes)

## 🎯 Overview

This service provides a complete integration with [payOS](https://payos.vn) payment gateway, implementing:

- **Create Payment Links** via payOS API (`POST /v2/payment-requests`)
- **Get Payment Status** (`GET /v2/payment-requests/{id}`)
- **Cancel Payment** (`POST /v2/payment-requests/{id}/cancel`)
- **Webhook handling** for payment notifications
- **HMAC_SHA256 signature** generation and verification
- **Idempotency** by `orderCode`
- **PostgreSQL** for persistence

All implementation follows the official payOS API documentation:
- https://payos.vn/docs/api/#tag/payment-request
- https://payos.vn/docs/api/#tag/payout/operation/create-single-payout

## ✨ Features

- ✅ Full payOS API integration (create, get, cancel payment)
- ✅ HMAC_SHA256 signature generation as per docs
- ✅ Idempotency by `orderCode` - duplicate requests return existing payment
- ✅ PostgreSQL database with JPA/Hibernate
- ✅ Comprehensive error handling
- ✅ Audit trail with `PaymentEvent` table
- ✅ Mock client for testing without calling payOS
- ✅ Unit tests for signature, service, and mock client
- ✅ Webhook endpoint (skeleton with documentation)
- ✅ Docker-ready with environment variable configuration

## 🛠 Technology Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **PostgreSQL** (production database)
- **Lombok** (reduce boilerplate)
- **JUnit 5 + Mockito** (testing)
- **Gradle** (build tool)

## 📦 Prerequisites

Before running this application, ensure you have:

1. **Java 21** installed
2. **PostgreSQL** database running
3. **payOS Account** with:
   - Client ID (`x-client-id`)
   - API Key (`x-api-key`)
   - Checksum Key (for signature generation)
   - (Optional) Partner Code

Get your credentials from: https://my.payos.vn

## 🔐 Environment Variables

### Required Environment Variables

Export these before running the application:

```bash
# Database Configuration
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=payment_db
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password

# payOS Credentials (REQUIRED)
export PAYOS_CLIENT_ID=your_client_id_here
export PAYOS_API_KEY=your_api_key_here
export PAYOS_CHECKSUM_KEY=your_checksum_key_here
```

### Optional Environment Variables

```bash
# payOS Optional
export PAYOS_API_BASE_URL=https://api-merchant.payos.vn  # default
export PAYOS_PARTNER_CODE=your_partner_code               # if participating in partner program
export PAYOS_RETURN_URL=http://localhost:8080/payment/result
export PAYOS_CANCEL_URL=http://localhost:8080/payment/cancel

# For testing with mock client
export PAYOS_MOCK_ENABLED=true

# Server
export SERVER_PORT=8080  # default
```

### Environment Variables Summary Table

| Variable | Required | Description | Default |
|----------|----------|-------------|---------|
| `DB_HOST` | ✅ Yes | PostgreSQL host | `localhost` |
| `DB_PORT` | ✅ Yes | PostgreSQL port | `5432` |
| `DB_NAME` | ✅ Yes | Database name | `payment_db` |
| `DB_USERNAME` | ✅ Yes | Database username | - |
| `DB_PASSWORD` | ✅ Yes | Database password | - |
| `PAYOS_CLIENT_ID` | ✅ Yes | payOS Client ID (x-client-id header) | - |
| `PAYOS_API_KEY` | ✅ Yes | payOS API Key (x-api-key header) | - |
| `PAYOS_CHECKSUM_KEY` | ✅ Yes | payOS Checksum Key for signature | - |
| `PAYOS_PARTNER_CODE` | ❌ No | Partner code (x-partner-code header) | - |
| `PAYOS_API_BASE_URL` | ❌ No | payOS API base URL | `https://api-merchant.payos.vn` |
| `PAYOS_RETURN_URL` | ❌ No | Default return URL on success | `http://localhost:8080/payment/result` |
| `PAYOS_CANCEL_URL` | ❌ No | Default cancel URL | `http://localhost:8080/payment/cancel` |
| `PAYOS_MOCK_ENABLED` | ❌ No | Use mock client instead of real API | `false` |
| `SERVER_PORT` | ❌ No | Server port | `8080` |

## 🚀 Setup & Installation

### 1. Clone the repository

```bash
cd payment-service
```

### 2. Create PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE payment_db;

# Create user (if needed)
CREATE USER payment_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;
```

### 3. Set Environment Variables

```bash
# Copy and edit the environment file
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=payment_db
export DB_USERNAME=payment_user
export DB_PASSWORD=your_password

# Set payOS credentials (get from https://my.payos.vn)
export PAYOS_CLIENT_ID=your_actual_client_id
export PAYOS_API_KEY=your_actual_api_key
export PAYOS_CHECKSUM_KEY=your_actual_checksum_key
```

### 4. Build the Application

```bash
./gradlew clean build
```

### 5. Run Tests

```bash
./gradlew test
```

## 🏃 Running the Application

### Using Gradle

```bash
./gradlew bootRun
```

### Using Java JAR

```bash
./gradlew bootJar
java -jar build/libs/payment-service-0.0.1-SNAPSHOT.jar
```

### Verify Application is Running

```bash
curl http://localhost:8080/actuator/health
```

Expected output:
```json
{"status":"UP"}
```

## 📡 API Documentation

### 1. Create Payment

Create a new payment link with payOS.

**Endpoint:** `POST /api/payments`

**Request Body:**

```json
{
  "orderCode": 123456,
  "amount": 50000,
  "description": "Payment for Order #123",
  "cancelUrl": "http://myapp.com/payment/cancel",
  "returnUrl": "http://myapp.com/payment/success",
  "expiredAt": 1699999999,
  "buyer": {
    "name": "Nguyen Van A",
    "email": "nguyenvana@example.com",
    "phone": "0912345678"
  },
  "items": [
    {
      "name": "Product 1",
      "quantity": 2,
      "price": 25000,
      "unit": "cái"
    }
  ]
}
```

**Required Fields:**
- `orderCode` (Long) - Unique order code
- `amount` (Long) - Amount in VND
- `description` (String) - Payment description
- `cancelUrl` (String) - URL when user cancels
- `returnUrl` (String) - URL when payment succeeds

**Optional Fields:**
- `expiredAt` (Long) - Unix timestamp when link expires
- `buyer` (BuyerDto) - Buyer information for invoice
- `items` (List<ItemDto>) - List of items
- `invoice` (InvoiceDto) - Invoice settings

**Response (201 Created):**

```json
{
  "paymentId": 1,
  "orderCode": 123456,
  "amount": 50000,
  "description": "Payment for Order #123",
  "checkoutUrl": "https://pay.payos.vn/web/abc123xyz",
  "paymentLinkId": "abc123xyz",
  "status": "PENDING",
  "expiredAt": 1699999999
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderCode": 123456,
    "amount": 50000,
    "description": "Test Payment",
    "cancelUrl": "http://localhost:8080/cancel",
    "returnUrl": "http://localhost:8080/success"
  }'
```

### 2. Get Payment by Order Code

Retrieve payment information.

**Endpoint:** `GET /api/payments/{orderCode}`

**Example:**

```bash
curl http://localhost:8080/api/payments/123456
```

**Response (200 OK):**

```json
{
  "paymentId": 1,
  "orderCode": 123456,
  "amount": 50000,
  "description": "Test Payment",
  "checkoutUrl": "https://pay.payos.vn/web/abc123xyz",
  "paymentLinkId": "abc123xyz",
  "status": "PENDING"
}
```

### 3. Webhook Endpoint (Skeleton)

Receive payment notifications from payOS.

**Endpoint:** `POST /api/payments/webhook`

**Payload from payOS:**

```json
{
  "code": "00",
  "desc": "success",
  "success": true,
  "data": {
    "orderCode": 123456,
    "amount": 50000,
    "description": "Test Payment",
    "accountNumber": "12345678",
    "reference": "TF230204212323",
    "transactionDateTime": "2023-02-04 18:25:00",
    "currency": "VND",
    "paymentLinkId": "abc123xyz",
    "code": "00",
    "desc": "Thành công"
  },
  "signature": "8d8640d802576397a1ce45ebda7f835055768ac7ad2e0bfb77f9b8f12cca4c7f"
}
```

**Implementation Notes:**

The webhook endpoint is currently a skeleton. To complete implementation:

1. Verify signature using `SignatureHelper.verifyWebhookSignature()`
2. Find payment by `paymentLinkId`
3. Update payment status to `PAID` or `FAILED`
4. Save webhook payload as `PaymentEvent`
5. Return `200 OK`

See comments in `PaymentController.handleWebhook()` for details.

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests SignatureHelperTest
./gradlew test --tests PaymentServiceTest
./gradlew test --tests PayosClientMockTest
```

### Test Coverage

The project includes tests for:

- ✅ **SignatureHelper** - Signature generation with known vectors
- ✅ **PaymentService** - Business logic, idempotency, error handling
- ✅ **PayosClientMock** - Mock client behavior for integration tests

### Using Mock Client for Testing

Set environment variable to use mock client:

```bash
export PAYOS_MOCK_ENABLED=true
./gradlew bootRun
```

The mock client stores payments in-memory and simulates payOS responses without calling the actual API.

## 🔐 Signature Generation

### How Signature Works

According to payOS documentation, the signature is generated as follows:

1. **Build data string** with parameters in **alphabetical order**:
   ```
   amount=50000&cancelUrl=http://test.com/cancel&description=Test&orderCode=123&returnUrl=http://test.com/return
   ```

2. **Compute HMAC_SHA256**:
   ```java
   signature = HMAC_SHA256(dataString, CHECKSUM_KEY)
   ```

3. **Convert to lowercase hex string** (64 characters)

### Example Signature Calculation

```java
import com.pbl6.payment.util.SignatureHelper;

String signature = SignatureHelper.generatePaymentSignature(
    50000L,                              // amount
    "http://test.com/cancel",            // cancelUrl
    "Payment for order #123",            // description
    123L,                                // orderCode
    "http://test.com/return",            // returnUrl
    "your_checksum_key_here"             // checksumKey from ENV
);

System.out.println(signature); // e.g., "a3f2b1c5..."
```

### Manual Signature Verification

You can verify the signature manually:

```bash
# Data string (alphabetically sorted)
DATA="amount=50000&cancelUrl=http://test.com/cancel&description=Test&orderCode=123&returnUrl=http://test.com/return"
KEY="your_checksum_key"

# Compute HMAC_SHA256
echo -n "$DATA" | openssl dgst -sha256 -hmac "$KEY"
```

## 🪝 Webhook Integration

### Configuring Webhook URL in payOS

1. Go to https://my.payos.vn
2. Navigate to your payment channel settings
3. Set webhook URL to: `https://your-domain.com/api/payments/webhook`
4. payOS will send a test webhook to verify

### Webhook Signature Verification Flow

```java
// 1. Build data string from webhook payload (alphabetically sorted)
String dataString = buildWebhookDataString(webhookData);

// 2. Verify signature
boolean isValid = SignatureHelper.verifyWebhookSignature(
    dataString,
    providedSignature,
    CHECKSUM_KEY
);

// 3. If valid, process webhook
if (isValid) {
    // Update payment status
    // Save event
    // Return 200 OK
} else {
    // Return 401 Unauthorized
}
```

See `PaymentController.handleWebhook()` for implementation stub.

## 🗄️ Database Schema

### `payments` Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key (auto-increment) |
| `order_code` | BIGINT | Unique order code from client |
| `amount` | BIGINT | Payment amount in VND |
| `description` | VARCHAR(1000) | Payment description |
| `status` | VARCHAR(20) | PENDING, PAID, CANCELLED, FAILED |
| `payos_payment_link_id` | VARCHAR(255) | payOS payment link ID |
| `checkout_url` | VARCHAR(500) | payOS checkout URL |
| `expired_at` | BIGINT | Expiration timestamp |
| `created_at` | TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | Last update timestamp |
| `meta` | TEXT | Additional JSON metadata |

**Indexes:**
- `idx_order_code` - Unique index on `order_code`
- `idx_payos_payment_link_id` - Index on `payos_payment_link_id`

### `payment_events` Table

Audit trail for all payOS API responses and webhooks.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `payment_id` | BIGINT | Reference to payment |
| `order_code` | BIGINT | Order code |
| `event_type` | VARCHAR(50) | CREATE_RESPONSE, WEBHOOK, etc. |
| `payload` | TEXT | Raw JSON response |
| `created_at` | TIMESTAMP | Event timestamp |

**Indexes:**
- `idx_payment_id` - Index on `payment_id`
- `idx_order_code` - Index on `order_code`

### Querying Database

```sql
-- View all payments
SELECT id, order_code, amount, status, checkout_url, created_at 
FROM payments 
ORDER BY created_at DESC;

-- View payment events
SELECT event_type, created_at, payload 
FROM payment_events 
WHERE order_code = 123456 
ORDER BY created_at DESC;

-- Check payment status
SELECT status, payos_payment_link_id 
FROM payments 
WHERE order_code = 123456;
```

## 📝 Assumptions & Notes

### Implementation Assumptions

1. **Database:** Using PostgreSQL instead of H2. Database credentials must be provided via environment variables.

2. **Signature Algorithm:** Implemented exactly as per payOS documentation:
   - Data format: `amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl`
   - Keys sorted alphabetically
   - HMAC_SHA256 with checksum key
   - Output as lowercase hex string

3. **Idempotency:** Payments are idempotent by `orderCode`. If a payment with the same `orderCode` exists and is PENDING or PAID, the existing payment is returned without calling payOS.

4. **Headers:** All payOS API calls include required headers:
   - `x-client-id` (required)
   - `x-api-key` (required)
   - `x-partner-code` (optional, only if provided)

5. **Error Handling:** 
   - payOS API errors are caught and mapped to meaningful exceptions
   - Failed payments are marked with status `FAILED`
   - All API responses are logged for debugging

6. **Webhook:** The webhook endpoint is a skeleton with detailed comments. Complete implementation requires:
   - Signature verification (algorithm documented in code)
   - Status update logic
   - Event logging

### Required Environment Variables for Production

Before deploying to production, ensure ALL required environment variables are set:

```bash
# Database (REQUIRED)
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD

# payOS Credentials (REQUIRED)
PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY
```

### Getting payOS Credentials

1. Register at https://my.payos.vn
2. Complete business/individual verification
3. Create a payment channel
4. Get credentials from channel settings:
   - **Client ID** → use for `PAYOS_CLIENT_ID`
   - **API Key** → use for `PAYOS_API_KEY`
   - **Checksum Key** → use for `PAYOS_CHECKSUM_KEY`

## 🐛 Troubleshooting

### Issue: "Missing required environment variable"

**Solution:** Ensure all required ENV vars are exported before running:

```bash
export PAYOS_CLIENT_ID=your_value
export PAYOS_API_KEY=your_value
export PAYOS_CHECKSUM_KEY=your_value
```

### Issue: Database connection failed

**Solution:** Verify PostgreSQL is running and credentials are correct:

```bash
psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
```

### Issue: Signature mismatch in payOS

**Solution:** 
1. Verify `PAYOS_CHECKSUM_KEY` matches your channel's checksum key
2. Check data string format is exactly as documented
3. Run unit test `SignatureHelperTest` to verify signature logic

### Issue: payOS API returns 401 Unauthorized

**Solution:** Verify `PAYOS_CLIENT_ID` and `PAYOS_API_KEY` are correct

## 📚 Additional Resources

- payOS Official Documentation: https://payos.vn/docs/
- payOS API Reference: https://payos.vn/docs/api/
- payOS Dashboard: https://my.payos.vn
- Webhook Integration Guide: https://payos.vn/docs/tich-hop-webhook/

## 📄 License

This is a demo implementation for educational purposes.

---

**Built with ❤️ following payOS API Documentation**
