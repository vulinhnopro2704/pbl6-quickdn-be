# Error Handling Documentation

## Tổng quan

Hệ thống xử lý lỗi toàn cục (Global Exception Handler) đã được cấu hình để đảm bảo tất cả các lỗi được trả về theo chuẩn API với format nhất quán:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/register",
  "validationErrors": {
    "field1": "error message 1",
    "field2": "error message 2"
  }
}
```

## Cấu trúc Files

### 1. Exception Classes (`src/main/java/com/pbl6/auth/exception/`)

- **`AuthException.java`**: Base exception class cho tất cả auth-related errors
- **`BadRequestException.java`**: Lỗi 400 - Bad Request
- **`UnauthorizedException.java`**: Lỗi 401 - Unauthorized
- **`ResourceNotFoundException.java`**: Lỗi 404 - Not Found

### 2. Global Exception Handler

**`GlobalExceptionHandler.java`**: RestControllerAdvice xử lý tất cả exceptions

#### Các loại lỗi được xử lý:

1. **Custom Exceptions** (AuthException và subclasses)
2. **Validation Errors** (MethodArgumentNotValidException)
3. **Spring Security Exceptions**:
   - BadCredentialsException
   - AuthenticationException
   - AccessDeniedException
4. **JWT Exceptions**:
   - ExpiredJwtException
   - MalformedJwtException
   - SignatureException
5. **General Exceptions**:
   - IllegalArgumentException
   - MethodArgumentTypeMismatchException
   - Exception (catch-all)

### 3. Error Response DTO

**`ErrorResponse.java`**: Record class định nghĩa cấu trúc response lỗi

## Cách sử dụng

### 1. Trong Service Layer

```java
// Bad Request (400)
throw new BadRequestException("Phone number already exists");

// Unauthorized (401)
throw new UnauthorizedException("Invalid phone number or password");

// Not Found (404)
throw new ResourceNotFoundException("User not found");
```

### 2. Validation với Bean Validation

Thêm annotations vào DTO:

```java
public record RegisterRequest(
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Invalid phone number format")
    String phone,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName
) {}
```

Trong Controller, thêm `@Valid`:

```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    // ...
}
```

## Response Examples

### 1. Bad Request - Validation Error

**Request:**
```json
POST /api/auth/register
{
  "phone": "123",
  "password": "short",
  "fullName": ""
}
```

**Response:** (400)
```json
{
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "status": 400,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/register",
  "validationErrors": {
    "phone": "Invalid phone number format",
    "password": "Password must be at least 8 characters",
    "fullName": "Full name is required"
  }
}
```

### 2. Bad Request - Phone Already Exists

**Response:** (400)
```json
{
  "error": "Bad Request",
  "message": "Phone number already exists",
  "status": 400,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/register"
}
```

### 3. Unauthorized - Invalid Credentials

**Response:** (401)
```json
{
  "error": "Unauthorized",
  "message": "Invalid phone number or password",
  "status": 401,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/login"
}
```

### 4. Unauthorized - Expired Token

**Response:** (401)
```json
{
  "error": "Unauthorized",
  "message": "Token has expired",
  "status": 401,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/refresh"
}
```

### 5. Internal Server Error

**Response:** (500)
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "status": 500,
  "timestamp": "2025-11-13T10:30:45",
  "path": "/api/auth/login"
}
```

## Best Practices

### 1. Sử dụng Custom Exceptions

✅ **DO:**
```java
if (userRepo.existsByPhone(phone)) {
    throw new BadRequestException("Phone number already exists");
}
```

❌ **DON'T:**
```java
if (userRepo.existsByPhone(phone)) {
    throw new RuntimeException("Phone already registered");
}
```

### 2. Validation Messages

- Sử dụng messages rõ ràng, dễ hiểu
- Không expose sensitive information
- Hỗ trợ đa ngôn ngữ (i18n) nếu cần

### 3. HTTP Status Codes

- **400 Bad Request**: Input không hợp lệ, validation errors
- **401 Unauthorized**: Authentication failed, token issues
- **403 Forbidden**: Không có quyền truy cập
- **404 Not Found**: Resource không tồn tại
- **500 Internal Server Error**: Lỗi server không mong đợi

### 4. Logging

- Custom exceptions: Log level ERROR
- Validation errors: Log level ERROR
- Unexpected errors: Log level ERROR với full stack trace

## Testing

### Test với cURL

```bash
# Test validation error
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "123",
    "password": "short",
    "fullName": ""
  }'

# Test phone already exists
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "0912345678",
    "password": "SecurePass123!",
    "fullName": "Nguyen Van A"
  }'

# Test invalid credentials
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "0912345678",
    "password": "wrongpassword"
  }'
```

## Tích hợp với OpenAPI/Swagger

Error responses đã được document trong OpenAPI annotations trong `AuthController.java`. Truy cập Swagger UI để xem chi tiết:

```
http://localhost:8081/swagger-ui.html
```

## Mở rộng

### Thêm Exception mới

1. Tạo class extends `AuthException`:

```java
package com.pbl6.auth.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AuthException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
```

2. Sử dụng trong code:

```java
throw new ConflictException("Resource already exists");
```

### Thêm Handler cho Exception Type mới

Trong `GlobalExceptionHandler.java`, thêm method:

```java
@ExceptionHandler(YourCustomException.class)
public ResponseEntity<ErrorResponse> handleYourCustomException(
        YourCustomException ex,
        HttpServletRequest request
) {
    logger.error("Custom error: {}", ex.getMessage());
    
    ErrorResponse errorResponse = new ErrorResponse(
        "Your Error Type",
        ex.getMessage(),
        HttpStatus.YOUR_STATUS.value(),
        request.getRequestURI()
    );
    
    return ResponseEntity.status(HttpStatus.YOUR_STATUS).body(errorResponse);
}
```

## Tài liệu tham khảo

- [Spring Boot Error Handling](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
- [Bean Validation](https://beanvalidation.org/)
- [RFC 7807 - Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)
