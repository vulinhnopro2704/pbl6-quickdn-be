# 🔐 DANH SÁCH BIẾN MÔI TRƯỜNG

## 📌 GITHUB SECRETS

Vào: **Settings → Secrets and variables → Actions → New repository secret**

### 🔑 Registry & VPS Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `REGISTRY_USERNAME` | GitHub username | `vulinhnopro2704` |
| `REGISTRY_TOKEN` | GitHub Personal Access Token | `ghp_xxxxxxxxxxxx` |
| `VPS_HOST` | IP hoặc domain VPS | `123.45.67.89` |
| `VPS_USERNAME` | Username SSH | `root` hoặc `ubuntu` |
| `VPS_SSH_KEY` | Private SSH key | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `VPS_PORT` | Port SSH | `22` |

### 🌐 Service URLs Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `AUTH_SERVICE_URL` | URL của Auth Service | `http://auth-service:8081` |
| `GOONGMAP_SERVICE_URL` | URL của Goongmap Service | `http://goongmap-service:8082` |
| `ORDER_SERVICE_URL` | URL của Order Service | `http://order-service:8083` |
| `PAYMENT_SERVICE_URL` | URL của Payment Service | `http://payment-service:8084` |

### 🗄️ Database Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `AUTH_DB_NAME` | Tên database auth service | `auth_db` |
| `AUTH_DB_USER` | Username database auth | `auth_user` |
| `AUTH_DB_PASSWORD` | Password database auth | `SecurePassword123!@#` |
| `GOONGMAP_DB_NAME` | Tên database goongmap service | `goongmap_db` |
| `GOONGMAP_DB_USER` | Username database goongmap | `goongmap_user` |
| `GOONGMAP_DB_PASSWORD` | Password database goongmap | `SecurePassword456!@#` |
| `ORDER_DB_NAME` | Tên database order service | `order_db` |
| `ORDER_DB_USER` | Username database order | `order_user` |
| `ORDER_DB_PASSWORD` | Password database order | `SecurePassword789!@#` |
| `PAYMENT_DB_NAME` | Tên database payment service | `payment_db` |
| `PAYMENT_DB_USER` | Username database payment | `payment_user` |
| `PAYMENT_DB_PASSWORD` | Password database payment | `SecurePassword012!@#` |

### 🔐 JWT & API Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `JWT_SECRET` | JWT secret key (min 32 chars) | `7f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c...` |
| `JWT_ACCESS_EXP_MS` | JWT access token expiration (ms) | `3600000` (1 giờ) |
| `JWT_REFRESH_EXP_MS` | JWT refresh token expiration (ms) | `86400000` (24 giờ) |
| `GOONGMAP_API_KEY` | Goongmap API key | `your-goongmap-api-key` |
| `PAYOS_CLIENT_ID` | PayOS Client ID | `your-payos-client-id` |
| `PAYOS_API_KEY` | PayOS API Key | `your-payos-api-key` |
| `PAYOS_CHECKSUM_KEY` | PayOS Checksum Key | `your-payos-checksum-key` |
| `PAYOS_PARTNER_CODE` | PayOS Partner Code (optional) | `` |
| `PAYOS_API_BASE_URL` | PayOS API Base URL | `https://api-merchant.payos.vn` |
| `PAYOS_RETURN_URL` | PayOS Return URL | `https://yourdomain.com/payment/result` |
| `PAYOS_CANCEL_URL` | PayOS Cancel URL | `https://yourdomain.com/payment/cancel` |
| `PAYOS_MOCK_ENABLED` | PayOS Mock Mode | `false` |

### 🌐 CORS Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `CORS_ALLOWED_ORIGINS` | Allowed origins (phân cách bởi dấu phẩy) | `https://yourdomain.com,http://localhost:3000` |
| `CORS_ALLOWED_METHODS` | Allowed HTTP methods | `GET,POST,PUT,DELETE,OPTIONS` |
| `CORS_ALLOWED_HEADERS` | Allowed headers | `Content-Type,Authorization` |
| `CORS_EXPOSED_HEADERS` | Exposed headers | `Authorization` |
| `CORS_ALLOW_CREDENTIALS` | Allow credentials | `true` |

### ⚙️ Application Secrets (Bắt buộc)

| Tên Secret | Mô tả | Ví dụ |
|-----------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |

---

## 📊 TỔNG KẾT SECRETS

**Tổng cộng: 42 secrets**

- ✅ Registry & VPS: 6 secrets
- ✅ Service URLs: 4 secrets
- ✅ Database: 12 secrets (Auth, Goongmap, Order, Payment)
- ✅ JWT & API: 12 secrets (JWT + Goongmap + PayOS)
- ✅ CORS: 5 secrets
- ✅ Application: 1 secret
- ✅ Gateway & Payment Service: Luôn sẵn sàng

---

## 🔧 HƯỚNG DẪN TẠO SECRETS

## � HƯỚNG DẪN TẠO SECRETS

### 1. Tạo GitHub Personal Access Token (PAT)
1. https://github.com/settings/tokens
2. "Generate new token (classic)"
3. Chọn scopes: `write:packages`, `read:packages`, `repo`
4. Copy token → dùng cho `REGISTRY_TOKEN`

### 2. Tạo SSH Key cho VPS

```bash
# Trên VPS, chạy:
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions_key -N ""
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Copy private key
cat ~/.ssh/github_actions_key
# Copy toàn bộ output → dùng cho VPS_SSH_KEY
```

### 3. Tạo mật khẩu mạnh cho Database

```bash
# Cách 1: OpenSSL (Linux/Mac)
openssl rand -base64 24

# Cách 2: Online
# https://passwordsgenerator.net/
# Chọn: 24 characters, include symbols

# Tạo 3 passwords khác nhau cho:
# - AUTH_DB_PASSWORD
# - GOONGMAP_DB_PASSWORD  
# - ORDER_DB_PASSWORD
# - PAYMENT_DB_PASSWORD
```

### 4. Tạo JWT Secret

```bash
# Cách 1: OpenSSL hex (64 ký tự)
openssl rand -hex 32

# Cách 2: UUID
uuidgen

# Dùng cho JWT_SECRET
```

### 5. Lấy Goongmap API Key

1. Đăng ký tài khoản: https://account.goong.io/
2. Tạo API key mới
3. Copy và dùng cho `GOONGMAP_API_KEY`

### 6. Lấy PayOS API Credentials

1. Đăng ký tài khoản: https://payos.vn/
2. Vào Dashboard → API Keys
3. Copy các thông tin sau:
   - `PAYOS_CLIENT_ID`
   - `PAYOS_API_KEY`
   - `PAYOS_CHECKSUM_KEY`
4. Cấu hình Return URL và Cancel URL phù hợp với domain của bạn

---

## 📋 CHECKLIST THÊM SECRETS

Copy checklist này và tick khi đã thêm xong:

### Registry & VPS
- [ ] `REGISTRY_USERNAME` = `vulinhnopro2704` (hoặc username của bạn)
- [ ] `REGISTRY_TOKEN` = `ghp_xxxxx...`
- [ ] `VPS_HOST` = IP VPS của bạn
- [ ] `VPS_USERNAME` = `root` hoặc `ubuntu`
- [ ] `VPS_SSH_KEY` = Private key từ VPS
- [ ] `VPS_PORT` = `22`

### Service URLs
- [ ] `AUTH_SERVICE_URL` = `http://auth-service:8081`
- [ ] `GOONGMAP_SERVICE_URL` = `http://goongmap-service:8082`
- [ ] `ORDER_SERVICE_URL` = `http://order-service:8083`
- [ ] `PAYMENT_SERVICE_URL` = `http://payment-service:8084`

### Database - Auth Service
- [ ] `AUTH_DB_NAME` = `auth_db`
- [ ] `AUTH_DB_USER` = `auth_user`
- [ ] `AUTH_DB_PASSWORD` = Mật khẩu mạnh (tự tạo)

### Database - Goongmap Service
- [ ] `GOONGMAP_DB_NAME` = `goongmap_db`
- [ ] `GOONGMAP_DB_USER` = `goongmap_user`
- [ ] `GOONGMAP_DB_PASSWORD` = Mật khẩu mạnh (tự tạo, khác auth)

### Database - Order Service
- [ ] `ORDER_DB_NAME` = `order_db`
- [ ] `ORDER_DB_USER` = `order_user`
- [ ] `ORDER_DB_PASSWORD` = Mật khẩu mạnh (tự tạo, khác 2 cái trên)

### Database - Payment Service
- [ ] `PAYMENT_DB_NAME` = `payment_db`
- [ ] `PAYMENT_DB_USER` = `payment_user`
- [ ] `PAYMENT_DB_PASSWORD` = Mật khẩu mạnh (tự tạo, khác 3 cái trên)

### JWT & API
- [ ] `JWT_SECRET` = Chuỗi ngẫu nhiên 64 ký tự
- [ ] `JWT_ACCESS_EXP_MS` = `3600000`
- [ ] `JWT_REFRESH_EXP_MS` = `86400000`
- [ ] `GOONGMAP_API_KEY` = API key từ Goongmap
- [ ] `PAYOS_CLIENT_ID` = Client ID từ PayOS Dashboard
- [ ] `PAYOS_API_KEY` = API Key từ PayOS Dashboard
- [ ] `PAYOS_CHECKSUM_KEY` = Checksum Key từ PayOS Dashboard
- [ ] `PAYOS_PARTNER_CODE` = `` (để trống nếu không có)
- [ ] `PAYOS_API_BASE_URL` = `https://api-merchant.payos.vn`
- [ ] `PAYOS_RETURN_URL` = `https://yourdomain.com/payment/result`
- [ ] `PAYOS_CANCEL_URL` = `https://yourdomain.com/payment/cancel`
- [ ] `PAYOS_MOCK_ENABLED` = `false`

### CORS
- [ ] `CORS_ALLOWED_ORIGINS` = `https://yourdomain.com` (thay domain thật)
- [ ] `CORS_ALLOWED_METHODS` = `GET,POST,PUT,DELETE,OPTIONS`
- [ ] `CORS_ALLOWED_HEADERS` = `Content-Type,Authorization`
- [ ] `CORS_EXPOSED_HEADERS` = `Authorization`
- [ ] `CORS_ALLOW_CREDENTIALS` = `true`

### Application
- [ ] `SPRING_PROFILES_ACTIVE` = `prod`

---

## 🚀 SAU KHI THÊM SECRETS

**Bạn KHÔNG CẦN tạo file `.env` thủ công trên VPS nữa!**

GitHub Actions sẽ tự động:
1. ✅ Tạo folder `~/pbl6-backend`
2. ✅ Tạo file `.env` từ secrets
3. ✅ Copy `docker-compose.yml`
4. ✅ Deploy services (Auth, Goongmap, Order, Payment, Gateway)

Chỉ cần:
1. Thêm 42 secrets vào GitHub
2. Cài Docker trên VPS
3. Setup SSH key
4. Setup firewall
5. Login GitHub Registry

Phần còn lại để GitHub Actions lo! 🎉

---

## 📌 VPS .ENV FILE

⚠️ **LƯU Ý**: Bạn KHÔNG CẦN tạo file này thủ công nữa!

File này sẽ được GitHub Actions tự động tạo tại: `~/pbl6-backend/.env`

Nội dung sẽ giống như:

```env
#==========================================
# REGISTRY CONFIGURATION
#==========================================
REGISTRY_USERNAME=vulinhnopro2704

#==========================================
# SERVICE URLS CONFIGURATION
#==========================================
AUTH_SERVICE_URL=http://auth-service:8081
GOONGMAP_SERVICE_URL=http://goongmap-service:8082
ORDER_SERVICE_URL=http://order-service:8083

#==========================================
# DATABASE - AUTH SERVICE
#==========================================
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_user
AUTH_DB_PASSWORD=<MẬT KHẨU MẠNH TỐI THIỂU 16 KÝ TỰ>

#==========================================
# DATABASE - GOONGMAP SERVICE
#==========================================
GOONGMAP_DB_NAME=goongmap_db
GOONGMAP_DB_USER=goongmap_user
GOONGMAP_DB_PASSWORD=<MẬT KHẨU MẠNH TỐI THIỂU 16 KÝ TỰ>

#==========================================
# DATABASE - ORDER SERVICE
#==========================================
ORDER_DB_NAME=order_db
ORDER_DB_USER=order_user
ORDER_DB_PASSWORD=<MẬT KHẨU MẠNH TỐI THIỂU 16 KÝ TỰ>

#==========================================
# JWT CONFIGURATION
#==========================================
JWT_SECRET=<CHUỖI BÍ MẬT TỐI THIỂU 32 KÝ TỰ>
JWT_ACCESS_EXP_MS=3600000
JWT_REFRESH_EXP_MS=86400000

#==========================================
# API KEYS
#==========================================
GOONGMAP_API_KEY=<API KEY TỪ GOONGMAP>

#==========================================
# CORS CONFIGURATION
#==========================================
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Content-Type,Authorization
CORS_EXPOSED_HEADERS=Authorization
CORS_ALLOW_CREDENTIALS=true

#==========================================
# SPRING PROFILE
#==========================================
SPRING_PROFILES_ACTIVE=prod
```

---

## 📌 CHI TIẾT TỪNG BIẾN

### Registry Configuration

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `REGISTRY_USERNAME` | `vulinhnopro2704` | GitHub username, dùng để pull images |

### Database - Auth Service

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `AUTH_DB_NAME` | `auth_db` | Tên database cho auth service |
| `AUTH_DB_USER` | `auth_user` | Username database |
| `AUTH_DB_PASSWORD` | Tự đặt | ⚠️ Phải là mật khẩu mạnh! |

**Tạo mật khẩu mạnh:**
```bash
# Trên VPS hoặc Linux/Mac:
openssl rand -base64 24

# Hoặc dùng online:
# https://passwordsgenerator.net/
```

### Database - Goongmap Service

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `GOONGMAP_DB_NAME` | `goongmap_db` | Tên database cho goongmap service |
| `GOONGMAP_DB_USER` | `goongmap_user` | Username database |
| `GOONGMAP_DB_PASSWORD` | Tự đặt | ⚠️ Phải khác password auth! |

### Database - Order Service

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `ORDER_DB_NAME` | `order_db` | Tên database cho order service |
| `ORDER_DB_USER` | `order_user` | Username database |
| `ORDER_DB_PASSWORD` | Tự đặt | ⚠️ Phải khác 2 password trên! |

### JWT Configuration

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `JWT_SECRET` | Tự đặt | ⚠️ Chuỗi bí mật tối thiểu 32 ký tự |
| `JWT_ACCESS_EXP_MS` | `3600000` | 1 giờ = 3,600,000 ms |
| `JWT_REFRESH_EXP_MS` | `86400000` | 24 giờ = 86,400,000 ms |

**Tạo JWT Secret:**
```bash
# Trên VPS hoặc Linux/Mac:
openssl rand -hex 32

# Hoặc dùng UUID:
uuidgen
```

### API Keys

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `GOONGMAP_API_KEY` | Lấy từ Goongmap | https://account.goong.io/ |

**Cách lấy Goongmap API Key:**
1. Đăng ký tài khoản: https://account.goong.io/
2. Tạo API key mới
3. Copy và paste vào `.env`

### CORS Configuration

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `CORS_ALLOWED_ORIGINS` | `https://yourdomain.com` | ⚠️ Thay bằng domain thật |
| `CORS_ALLOWED_METHODS` | `GET,POST,PUT,DELETE,OPTIONS` | HTTP methods được phép |
| `CORS_ALLOWED_HEADERS` | `Content-Type,Authorization` | Headers được phép |
| `CORS_EXPOSED_HEADERS` | `Authorization` | Headers expose cho client |
| `CORS_ALLOW_CREDENTIALS` | `true` | Cho phép gửi credentials |

**Ví dụ CORS_ALLOWED_ORIGINS:**
```env
# Cho phép nhiều domains
CORS_ALLOWED_ORIGINS=https://app.yourdomain.com,https://admin.yourdomain.com,http://localhost:3000

# Development (cho phép tất cả - KHÔNG DÙNG PRODUCTION!)
CORS_ALLOWED_ORIGINS=*
```

### Spring Profile

| Biến | Giá trị | Ghi chú |
|------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Profile: `dev`, `staging`, `prod` |

---

## 📌 TEMPLATE ĐẦY ĐỦ

### File: `~/pbl6-backend/.env`

```env
REGISTRY_USERNAME=vulinhnopro2704
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_user
AUTH_DB_PASSWORD=A1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6
GOONGMAP_DB_NAME=goongmap_db
GOONGMAP_DB_USER=goongmap_user
GOONGMAP_DB_PASSWORD=Q9w8E7r6T5y4U3i2O1p0A9s8D7f6G5h4
ORDER_DB_NAME=order_db
ORDER_DB_USER=order_user
ORDER_DB_PASSWORD=Z1x2C3v4B5n6M7k8L9j0H1g2F3d4S5a6
JWT_SECRET=7f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0
JWT_ACCESS_EXP_MS=3600000
JWT_REFRESH_EXP_MS=86400000
GOONGMAP_API_KEY=abcdefgh123456789YOURREALAPIKEY
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com,http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Content-Type,Authorization
CORS_EXPOSED_HEADERS=Authorization
CORS_ALLOW_CREDENTIALS=true
SPRING_PROFILES_ACTIVE=prod
```

⚠️ **QUAN TRỌNG**: Thay tất cả các giá trị example bằng giá trị thật!

---

## 🔒 BẢO MẬT

### ✅ NÊN:
- Sử dụng mật khẩu mạnh (16+ ký tự, kết hợp chữ, số, ký tự đặc biệt)
- Mỗi service dùng mật khẩu database khác nhau
- Không commit file `.env` vào git
- Backup file `.env` an toàn
- Rotate credentials định kỳ (3-6 tháng)

### ❌ KHÔNG:
- Dùng mật khẩu đơn giản (123456, password, etc.)
- Share file `.env` qua email, chat
- Commit `.env` vào git repository
- Dùng chung mật khẩu cho nhiều services
- Để `CORS_ALLOWED_ORIGINS=*` trong production

---

## 🛠️ TOOLS HỮU ÍCH

### Tạo mật khẩu ngẫu nhiên:
```bash
# 24 ký tự base64
openssl rand -base64 24

# 32 ký tự hex
openssl rand -hex 32

# UUID
uuidgen
```

### Kiểm tra biến môi trường:
```bash
# Trên VPS
cd ~/pbl6-backend
cat .env

# Kiểm tra Docker Compose có load đúng không
docker compose config
```

### Test từng service:
```bash
# Test database connection
docker exec -it auth-postgres-db psql -U auth_user -d auth_db -c "SELECT version();"

# Test Redis
docker exec -it redis-jwt-blacklist redis-cli ping

# Test service health
curl http://localhost:8081/actuator/health
```

---

## ❓ FAQ

**Q: Tôi quên mật khẩu database?**
A: Vào file `.env` trên VPS xem lại: `cat ~/pbl6-backend/.env`

**Q: Làm sao thay đổi mật khẩu database?**
A:
```bash
# 1. Stop services
docker compose down

# 2. Xóa volumes (⚠️ MẤT DATA!)
docker volume rm pbl6-backend_auth-db-data

# 3. Sửa .env với password mới
nano .env

# 4. Start lại
docker compose up -d
```

**Q: CORS_ALLOWED_ORIGINS cần format như nào?**
A: Cách nhau bởi dấu phẩy, không có khoảng trắng:
```env
CORS_ALLOWED_ORIGINS=https://domain1.com,https://domain2.com
```

**Q: Tôi có thể dùng HTTP thay vì HTTPS không?**
A: Được, nhưng chỉ cho development:
```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://192.168.1.100:3000
```

---

Nếu còn thắc mắc, xem file `SETUP_GUIDE.md` hoặc `QUICK_SETUP.md` 🚀
