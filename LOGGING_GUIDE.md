# Logging System Documentation

## Overview
Hệ thống logging đầy đủ cho microservices architecture với Gateway và các services (Auth, Order, Payment).

## Features
✅ **Request/Response Logging** - Ghi log đầy đủ mọi request và response  
✅ **Vietnam Timezone** - Thời gian hiển thị theo múi giờ Việt Nam (Asia/Ho_Chi_Minh)  
✅ **Request ID Tracking** - Theo dõi request xuyên suốt các services  
✅ **Performance Metrics** - Đo thời gian xử lý (duration) cho mỗi request  
✅ **Client IP Detection** - Phát hiện IP thật của client qua X-Forwarded-For  
✅ **Service Routing Info** - Gateway log đích đến của từng request  
✅ **Status Code Tracking** - Theo dõi success/error với status code  
✅ **Pretty Format** - Log dạng box đẹp mắt, dễ đọc  
✅ **File Rotation** - Tự động rotate log files theo ngày và kích thước  
✅ **Async Logging** - Không ảnh hưởng performance nhờ async appenders  

## Log Format

### Gateway Log Example
```
╔═══════════════════════════════════════════════════════════════
║ [GATEWAY] INCOMING REQUEST
╠═══════════════════════════════════════════════════════════════
║ Request ID     : 550e8400-e29b-41d4-a716-446655440000
║ Time (VN)      : 2025-11-15 14:30:45.123
║ Client IP      : 192.168.1.100
║ Method         : POST
║ Path           : /api/auth/login
║ User-Agent     : Mozilla/5.0...
║ Forwarding to  : AUTH Service
╚═══════════════════════════════════════════════════════════════

╔═══════════════════════════════════════════════════════════════
║ [GATEWAY] OUTGOING RESPONSE - SUCCESS
╠═══════════════════════════════════════════════════════════════
║ Request ID     : 550e8400-e29b-41d4-a716-446655440000
║ Time (VN)      : 2025-11-15 14:30:45.456
║ Method         : POST
║ Path           : /api/auth/login
║ Target Service : AUTH
║ Status         : 200 OK ✓
║ Duration       : 333 ms
╚═══════════════════════════════════════════════════════════════
```

### Service Log Example
```
┌─────────────────────────────────────────────────────────────
│ [AUTH-SERVICE] REQUEST RECEIVED
├─────────────────────────────────────────────────────────────
│ Request ID     : 550e8400-e29b-41d4-a716-446655440000
│ Time (VN)      : 2025-11-15 14:30:45.134
│ Origin         : Gateway
│ Client IP      : 192.168.1.100
│ Method         : POST
│ Path           : /api/auth/login
│ Content-Type   : application/json
└─────────────────────────────────────────────────────────────

┌─────────────────────────────────────────────────────────────
│ [AUTH-SERVICE] RESPONSE SENT - SUCCESS
├─────────────────────────────────────────────────────────────
│ Request ID     : 550e8400-e29b-41d4-a716-446655440000
│ Time (VN)      : 2025-11-15 14:30:45.445
│ Method         : POST
│ Path           : /api/auth/login
│ Status         : 200 OK ✓
│ Duration       : 311 ms
└─────────────────────────────────────────────────────────────
```

## Log Files

### Gateway
- `logs/gateway.log` - Tất cả logs
- `logs/gateway-requests.log` - Chỉ request/response logs
- `logs/gateway-error.log` - Chỉ error logs

### Auth Service
- `logs/auth-service.log` - Tất cả logs
- `logs/auth-requests.log` - Chỉ request/response logs
- `logs/auth-error.log` - Chỉ error logs

### Order Service
- `logs/order-service.log` - Tất cả logs
- `logs/order-requests.log` - Chỉ request/response logs
- `logs/order-error.log` - Chỉ error logs

### Payment Service
- `logs/payment-service.log` - Tất cả logs
- `logs/payment-requests.log` - Chỉ request/response logs
- `logs/payment-error.log` - Chỉ error logs

## Log Rotation Policy

- **Max File Size**: 100MB (requests/all logs), 50MB (error logs)
- **Rotation**: Daily hoặc khi đạt max size
- **Retention**: 30 ngày (normal logs), 90 ngày (error logs)
- **Total Cap**: 3GB (normal), 1GB (error)

## Request ID Tracking

Request ID tự động được tạo và truyền qua các services:
1. Client gửi request đến Gateway (có thể có hoặc không có `X-Request-ID` header)
2. Gateway tạo hoặc sử dụng existing Request ID
3. Gateway forward request với `X-Request-ID` header đến service
4. Service nhận và log với cùng Request ID
5. Response trả về kèm `X-Request-ID` header

## Configuration

### Logging Levels
Điều chỉnh trong `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.pbl6.gateway: INFO
    com.pbl6.gateway.filter: INFO  # Request logging
    org.springframework.web: WARN
```

### Enable/Disable Request Logging
Để tắt request logging, thay đổi level:
```yaml
logging:
  level:
    com.pbl6.gateway.filter: OFF
```

## Performance Impact

- **Async Logging**: Sử dụng AsyncAppender để giảm thiểu impact
- **Queue Size**: 512 (có thể tăng nếu traffic cao)
- **Non-blocking**: Không block request processing
- **Estimated Overhead**: < 5ms per request

## Best Practices

### 1. Monitoring
```bash
# Theo dõi logs real-time
tail -f logs/gateway-requests.log

# Tìm kiếm theo Request ID
grep "550e8400-e29b-41d4-a716-446655440000" logs/*.log

# Lọc errors
grep "ERROR" logs/gateway.log
```

### 2. Log Analysis
```bash
# Đếm số requests theo status code
grep "Status" logs/gateway-requests.log | awk '{print $7}' | sort | uniq -c

# Tìm slow requests (> 1000ms)
grep "Duration" logs/gateway-requests.log | awk '$7 > 1000'

# Phân tích traffic theo service
grep "Forwarding to" logs/gateway-requests.log | awk '{print $7}' | sort | uniq -c
```

### 3. Production Deployment
- Sử dụng log aggregation tool (ELK Stack, Grafana Loki)
- Setup log forwarding từ các services
- Enable alerting cho error logs
- Monitor disk space cho log files

### 4. Security
- Không log sensitive data (passwords, tokens)
- Mask credit card numbers, personal info
- Limit log file permissions: `chmod 600 logs/*.log`

## Troubleshooting

### Issue: Log files không được tạo
**Solution**: Kiểm tra quyền ghi thư mục `logs/`
```bash
mkdir -p logs
chmod 755 logs
```

### Issue: Log files quá lớn
**Solution**: Giảm retention hoặc max file size trong `logback-spring.xml`

### Issue: Performance degradation
**Solution**: 
- Tăng async queue size
- Giảm log level cho non-critical packages
- Disable SQL logging trong production

## Example Use Cases

### 1. Debug Một Request Cụ Thể
```bash
# Tìm request ID từ client
grep "POST /api/order/create" logs/gateway-requests.log | grep "Request ID"

# Theo dõi request qua các services
grep "550e8400-..." logs/gateway-requests.log
grep "550e8400-..." logs/order-requests.log
```

### 2. Phân Tích Performance
```bash
# Tìm average response time
grep "Duration" logs/gateway-requests.log | awk '{sum+=$7; count++} END {print sum/count " ms"}'

# Top 10 slowest endpoints
grep "Duration" logs/gateway-requests.log | sort -k7 -n | tail -10
```

### 3. Monitor Error Rate
```bash
# Count errors trong 1 giờ qua
grep "$(date +%Y-%m-%d\ %H)" logs/gateway-error.log | wc -l

# Phân loại errors
grep "Error Type" logs/auth-error.log | awk '{print $6}' | sort | uniq -c
```

## Integration with Docker

Log files được persist trong Docker volumes và có thể truy cập từ container:

### Docker Compose Setup
```yaml
volumes:
  gateway-logs:
  auth-service-logs:
  order-service-logs:
  payment-service-logs:
  goongmap-service-logs:

services:
  gateway:
    environment:
      TZ: Asia/Ho_Chi_Minh  # Vietnam timezone
    volumes:
      - gateway-logs:/app/logs
```

### Timezone Configuration
Tất cả containers đã được configure với Vietnam timezone:
- **OS Level**: `/etc/localtime` → `Asia/Ho_Chi_Minh`
- **JVM Level**: `-Duser.timezone=Asia/Ho_Chi_Minh`
- **Container ENV**: `TZ=Asia/Ho_Chi_Minh`

### Accessing Logs from Docker

#### View logs in real-time
```bash
# Gateway logs
docker exec -it api-gateway tail -f /app/logs/gateway-requests.log

# Auth service logs
docker exec -it auth-service tail -f /app/logs/auth-requests.log

# Order service logs
docker exec -it order-service tail -f /app/logs/order-requests.log

# Payment service logs
docker exec -it payment-service tail -f /app/logs/payment-requests.log
```

#### Copy logs to host
```bash
# Copy all gateway logs
docker cp api-gateway:/app/logs ./local-logs/gateway

# Copy specific log file
docker cp auth-service:/app/logs/auth-error.log ./error-logs/
```

#### View logs using docker-compose
```bash
# Service stdout/stderr logs
docker-compose logs -f gateway
docker-compose logs -f auth-service

# Last 100 lines
docker-compose logs --tail=100 order-service
```

#### Access logs via volume inspection
```bash
# List all volumes
docker volume ls

# Inspect gateway logs volume
docker volume inspect pbl6-quickdn-be_gateway-logs

# Find volume mount point
docker volume inspect pbl6-quickdn-be_gateway-logs | grep Mountpoint
```

#### Backup logs from volumes
```bash
# Create backup directory
mkdir -p ./backups/logs

# Backup gateway logs
docker run --rm -v pbl6-quickdn-be_gateway-logs:/source -v $(pwd)/backups/logs:/backup alpine tar czf /backup/gateway-logs-$(date +%Y%m%d).tar.gz -C /source .

# Backup all service logs
for service in gateway auth-service order-service payment-service goongmap-service; do
  docker run --rm \
    -v pbl6-quickdn-be_${service}-logs:/source \
    -v $(pwd)/backups/logs:/backup \
    alpine tar czf /backup/${service}-logs-$(date +%Y%m%d).tar.gz -C /source .
done
```

### Docker Production Best Practices

#### 1. Log Rotation in Docker
Logs được rotate tự động bởi logback-spring.xml, nhưng bạn cũng nên configure Docker log driver:

```yaml
services:
  gateway:
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "5"
```

#### 2. Centralized Logging
Để dễ quản lý, forward logs đến centralized system:

```bash
# Using Loki driver
docker plugin install grafana/loki-docker-driver:latest --alias loki --grant-all-permissions

# Update docker-compose.yml
services:
  gateway:
    logging:
      driver: loki
      options:
        loki-url: "http://loki:3100/loki/api/v1/push"
        loki-batch-size: "400"
```

#### 3. Health Check Monitoring
Monitor logs cho errors:

```bash
# Check for errors in last hour
docker exec api-gateway sh -c "grep '$(date +%Y-%m-%d\ %H)' /app/logs/gateway-error.log | wc -l"

# Alert if too many errors
ERROR_COUNT=$(docker exec api-gateway grep -c "ERROR" /app/logs/gateway-error.log || echo 0)
if [ $ERROR_COUNT -gt 100 ]; then
  echo "⚠️  High error count: $ERROR_COUNT"
fi
```

#### 4. Timezone Verification
Kiểm tra timezone trong container:

```bash
# Check container timezone
docker exec api-gateway date
docker exec api-gateway cat /etc/timezone

# Verify JVM timezone
docker exec api-gateway java -XshowSettings:properties -version 2>&1 | grep user.timezone
```

Expected output:
```
Fri Nov 15 14:30:45 +07 2025
Asia/Ho_Chi_Minh
user.timezone = Asia/Ho_Chi_Minh
```
