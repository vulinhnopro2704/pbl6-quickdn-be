# 📋 GITHUB SECRETS TEMPLATE

Copy từng block bên dưới để thêm vào GitHub Secrets nhanh hơn.

**Đường dẫn**: Settings → Secrets and variables → Actions → New repository secret

---

## 🔐 Registry & VPS (6 secrets)

### REGISTRY_USERNAME
```
vulinhnopro2704
```

### REGISTRY_TOKEN
```
ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```
👆 Thay bằng GitHub Personal Access Token của bạn

### VPS_HOST
```
123.45.67.89
```
👆 Thay bằng IP VPS thực tế

### VPS_USERNAME
```
root
```
👆 Hoặc `ubuntu` nếu dùng user khác

### VPS_SSH_KEY
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
...
(copy toàn bộ private key từ VPS)
...
-----END OPENSSH PRIVATE KEY-----
```
👆 Chạy `cat ~/.ssh/github_actions_key` trên VPS và copy toàn bộ

### VPS_PORT
```
22
```

---

## 🌐 Service URLs (3 secrets)

### AUTH_SERVICE_URL
```
http://auth-service:8081
```
👆 URL của auth service (trong Docker network)

### GOONGMAP_SERVICE_URL
```
http://goongmap-service:8082
```
👆 URL của goongmap service (trong Docker network)

### ORDER_SERVICE_URL
```
http://order-service:8083
```
👆 URL của order service (trong Docker network)

---

## 🗄️ Database (9 secrets)

### AUTH_DB_NAME
```
auth_db
```

### AUTH_DB_USER
```
auth_user
```

### AUTH_DB_PASSWORD
```
A1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6
```
👆 Tạo mật khẩu mạnh: `openssl rand -base64 24`

### GOONGMAP_DB_NAME
```
goongmap_db
```

### GOONGMAP_DB_USER
```
goongmap_user
```

### GOONGMAP_DB_PASSWORD
```
Q9w8E7r6T5y4U3i2O1p0A9s8D7f6G5h4
```
👆 Tạo mật khẩu mạnh khác: `openssl rand -base64 24`

### ORDER_DB_NAME
```
order_db
```

### ORDER_DB_USER
```
order_user
```

### ORDER_DB_PASSWORD
```
Z1x2C3v4B5n6M7k8L9j0H1g2F3d4S5a6
```
👆 Tạo mật khẩu mạnh khác: `openssl rand -base64 24`

---

## 🔐 JWT & API (4 secrets)

### JWT_SECRET
```
7f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8
```
👆 Tạo chuỗi ngẫu nhiên 64 ký tự: `openssl rand -hex 32`

### JWT_ACCESS_EXP_MS
```
3600000
```
👆 1 giờ (3,600,000 milliseconds)

### JWT_REFRESH_EXP_MS
```
86400000
```
👆 24 giờ (86,400,000 milliseconds)

### GOONGMAP_API_KEY
```
your-goongmap-api-key-here
```
👆 Lấy từ https://account.goong.io/

---

## 🌐 CORS (5 secrets)

### CORS_ALLOWED_ORIGINS
```
https://yourdomain.com,https://www.yourdomain.com,http://localhost:3000
```
👆 Thay bằng domain thật của frontend (phân cách bởi dấu phẩy, KHÔNG có khoảng trắng)

### CORS_ALLOWED_METHODS
```
GET,POST,PUT,DELETE,OPTIONS
```

### CORS_ALLOWED_HEADERS
```
Content-Type,Authorization
```

### CORS_EXPOSED_HEADERS
```
Authorization
```

### CORS_ALLOW_CREDENTIALS
```
true
```

---

## ⚙️ Application (1 secret)

### SPRING_PROFILES_ACTIVE
```
prod
```

---

## ✅ CHECKLIST

Copy checklist này và tick khi hoàn thành:

### Registry & VPS
- [ ] REGISTRY_USERNAME
- [ ] REGISTRY_TOKEN
- [ ] VPS_HOST
- [ ] VPS_USERNAME
- [ ] VPS_SSH_KEY
- [ ] VPS_PORT

### Service URLs
- [ ] AUTH_SERVICE_URL
- [ ] GOONGMAP_SERVICE_URL
- [ ] ORDER_SERVICE_URL

### Database - Auth
- [ ] AUTH_DB_NAME
- [ ] AUTH_DB_USER
- [ ] AUTH_DB_PASSWORD

### Database - Goongmap
- [ ] GOONGMAP_DB_NAME
- [ ] GOONGMAP_DB_USER
- [ ] GOONGMAP_DB_PASSWORD

### Database - Order
- [ ] ORDER_DB_NAME
- [ ] ORDER_DB_USER
- [ ] ORDER_DB_PASSWORD

### JWT & API
- [ ] JWT_SECRET
- [ ] JWT_ACCESS_EXP_MS
- [ ] JWT_REFRESH_EXP_MS
- [ ] GOONGMAP_API_KEY

### CORS
- [ ] CORS_ALLOWED_ORIGINS
- [ ] CORS_ALLOWED_METHODS
- [ ] CORS_ALLOWED_HEADERS
- [ ] CORS_EXPOSED_HEADERS
- [ ] CORS_ALLOW_CREDENTIALS

### Application
- [ ] SPRING_PROFILES_ACTIVE

---

## 🛠️ TOOLS TẠO MẬT KHẨU

```bash
# Mật khẩu database (24 ký tự base64)
openssl rand -base64 24

# JWT Secret (64 ký tự hex)
openssl rand -hex 32

# UUID (alternative cho JWT secret)
uuidgen
```

---

## 🎯 SAU KHI THÊM SECRETS

1. ✅ Đảm bảo đã thêm đủ 26 secrets
2. ✅ Verify tên secrets chính xác (phân biệt chữ hoa/thường)
3. ✅ Push code lên GitHub
4. ✅ Xem GitHub Actions workflow chạy
5. ✅ Check VPS xem file `.env` đã được tạo tự động

**GitHub Actions sẽ tự động tạo file `.env` trên VPS!** 🎉
