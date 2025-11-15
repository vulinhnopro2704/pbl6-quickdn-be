# 🚀 HƯỚNG DẪN SETUP CI/CD - TỪNG BƯỚC

## 📋 MỤC LỤC
1. [Setup GitHub](#1-setup-github)
2. [Setup VPS Ubuntu 22.04](#2-setup-vps-ubuntu-2204)
3. [Kiểm tra và Test](#3-kiểm-tra-và-test)

---

## 1️⃣ SETUP GITHUB

### Bước 1.1: Tạo GitHub Personal Access Token (PAT)

1. Truy cập: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Đặt tên token: `GitHub Actions - Container Registry`
4. Chọn thời hạn: **No expiration** (hoặc 90 days)
5. Chọn các quyền sau:
   - ✅ `write:packages` (tự động chọn `read:packages` và `delete:packages`)
   - ✅ `repo` (nếu repo là private)
6. Click **"Generate token"**
7. **QUAN TRỌNG**: Copy token ngay (bắt đầu bằng `ghp_...`), bạn sẽ không thấy lại!

### Bước 1.2: Thêm Secrets vào GitHub Repository

1. Vào repository: https://github.com/vulinhnopro2704/PBL6-QUICKDN-BACKEND
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"** và thêm từng secret sau:

#### ✅ Danh sách Secrets cần thêm:

| Secret Name | Giá trị | Ví dụ |
|------------|---------|-------|
| `REGISTRY_USERNAME` | GitHub username của bạn | `vulinhnopro2704` |
| `REGISTRY_TOKEN` | Token vừa tạo ở bước 1.1 | `ghp_xxxxxxxxxxxxxxxxxxxx` |
| `VPS_HOST` | IP hoặc domain VPS | `123.45.67.89` |
| `VPS_USERNAME` | Username SSH của VPS | `root` hoặc `ubuntu` |
| `VPS_SSH_KEY` | Private SSH key (tạo ở bước 2.2) | `-----BEGIN OPENSSH...` |
| `VPS_PORT` | Port SSH | `22` |
| `DOCKER_COMPOSE_PATH` | Đường dẫn project trên VPS | `/opt/pbl6-backend` |

**Lưu ý**: Để trống `VPS_SSH_KEY` tạm thời, sẽ điền sau khi setup VPS ở bước 2.2

---

## 2️⃣ SETUP VPS UBUNTU 22.04

### Bước 2.1: SSH vào VPS và Update hệ thống

```bash
# SSH vào VPS (từ máy local)
ssh root@YOUR_VPS_IP
# Hoặc nếu dùng user khác
ssh ubuntu@YOUR_VPS_IP

# Update system
sudo apt update && sudo apt upgrade -y
```

### Bước 2.2: Tạo SSH Key cho GitHub Actions

**Trên VPS**, chạy lệnh sau:

```bash
# Tạo thư mục .ssh nếu chưa có
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Tạo SSH key mới
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_key -N ""

# Thêm public key vào authorized_keys
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# In ra private key
echo "====== COPY PRIVATE KEY BÊN DƯỚI ======"
cat ~/.ssh/github_actions_key
echo "====== END PRIVATE KEY ======"
```

**QUAN TRỌNG**: 
- Copy **toàn bộ** nội dung private key (bao gồm `-----BEGIN OPENSSH PRIVATE KEY-----` và `-----END OPENSSH PRIVATE KEY-----`)
- Quay lại GitHub, vào Settings → Secrets → Edit `VPS_SSH_KEY`
- Paste toàn bộ nội dung vừa copy vào

### Bước 2.3: Cài đặt Docker và Docker Compose

```bash
# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Nếu không dùng root, thêm user vào docker group
sudo usermod -aG docker $USER

# Logout và login lại để apply
exit
# SSH lại vào VPS
ssh YOUR_USER@YOUR_VPS_IP

# Verify Docker
docker --version
# Output: Docker version 24.x.x

# Verify Docker Compose
docker compose version
# Output: Docker Compose version v2.x.x
```

### Bước 2.4: Tạo thư mục project

```bash
# Tạo thư mục
sudo mkdir -p /opt/pbl6-backend
sudo chown -R $USER:$USER /opt/pbl6-backend
cd /opt/pbl6-backend

# Kiểm tra
pwd
# Output: /opt/pbl6-backend
```

### Bước 2.5: Tạo file docker-compose.yml

```bash
cd /opt/pbl6-backend

# Tạo file docker-compose.yml
nano docker-compose.yml
```

**Copy nội dung từ file `docker-compose.yml` trong repo và paste vào**, sau đó:
- Nhấn `Ctrl + O` để save
- Nhấn `Enter` để confirm
- Nhấn `Ctrl + X` để thoát

**HOẶC** clone repository và copy file:

```bash
cd /opt/pbl6-backend

# Clone repo (nếu repo là public)
git clone https://github.com/vulinhnopro2704/PBL6-QUICKDN-BACKEND.git temp
cp temp/docker-compose.yml docker-compose.yml
rm -rf temp

# Xem nội dung để verify
cat docker-compose.yml
```

### Bước 2.6: Tạo file .env

```bash
cd /opt/pbl6-backend

# Tạo file .env
nano .env
```

**Copy và điền các giá trị thực tế:**

```bash
# Registry Configuration
REGISTRY_USERNAME=vulinhnopro2704

# Database Configuration - Auth Service
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_user
AUTH_DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE_123

# Database Configuration - Goongmap Service
GOONGMAP_DB_NAME=goongmap_db
GOONGMAP_DB_USER=goongmap_user
GOONGMAP_DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE_456

# Database Configuration - Order Service
ORDER_DB_NAME=order_db
ORDER_DB_USER=order_user
ORDER_DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE_789

# JWT Configuration
JWT_SECRET=cd6d047b-f53a-439d-8412-d61c2ce4a743-vulinhnopro2704pbl6-production
JWT_ACCESS_EXP_MS=3600000
JWT_REFRESH_EXP_MS=86400000

# API Keys
GOONGMAP_API_KEY=YOUR_GOONGMAP_API_KEY_HERE

# CORS Configuration (thay bằng domain thật của bạn)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Content-Type,Authorization
CORS_EXPOSED_HEADERS=Authorization
CORS_ALLOW_CREDENTIALS=true

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**Lưu ý**: Thay thế các giá trị sau:
- `YOUR_SECURE_PASSWORD_HERE_*`: Mật khẩu database mạnh (ít nhất 16 ký tự)
- `YOUR_GOONGMAP_API_KEY_HERE`: API key của Goongmap
- `CORS_ALLOWED_ORIGINS`: Domain frontend thực tế

Sau đó save: `Ctrl + O` → `Enter` → `Ctrl + X`

### Bước 2.7: Login GitHub Container Registry

```bash
# Thay YOUR_GITHUB_USERNAME và YOUR_GITHUB_TOKEN
echo "YOUR_GITHUB_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin

# Ví dụ:
# echo "ghp_xxxxxxxxxxxx" | docker login ghcr.io -u vulinhnopro2704 --password-stdin

# Verify login
cat ~/.docker/config.json
# Phải thấy "ghcr.io" trong đó
```

### Bước 2.8: Cấu hình Firewall (UFW)

```bash
# Enable firewall
sudo ufw enable

# Allow SSH (QUAN TRỌNG - không bị lock khỏi VPS)
sudo ufw allow 22/tcp

# Allow HTTP và HTTPS (cho production sau này)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Allow các port của services (optional - nếu muốn truy cập trực tiếp)
sudo ufw allow 8080/tcp  # Gateway
sudo ufw allow 8081/tcp  # Auth service
sudo ufw allow 8082/tcp  # Goongmap service
sudo ufw allow 8083/tcp  # Order service

# Check status
sudo ufw status

# Reload
sudo ufw reload
```

### Bước 2.9: Test pull image thủ công (Optional nhưng recommended)

```bash
cd /opt/pbl6-backend

# Test pull một image (sẽ fail nếu chưa có image trên registry)
docker pull ghcr.io/vulinhnopro2704/pbl6-quickdn-be/auth-service:latest

# Nếu lỗi "not found" là bình thường vì chưa build lần đầu
# Bỏ qua bước này, sẽ có image sau khi GitHub Actions chạy lần đầu
```

---

## 3️⃣ KIỂM TRA VÀ TEST

### Bước 3.1: Test GitHub Actions lần đầu

1. **Commit và push code lên GitHub**:

```bash
# Trên máy local (trong thư mục project)
git add .
git commit -m "Setup CI/CD with GitHub Actions"
git push origin main
```

2. **Xem workflow chạy**:
   - Vào: https://github.com/vulinhnopro2704/PBL6-QUICKDN-BACKEND/actions
   - Sẽ thấy workflow "Build and Deploy Services" đang chạy
   - Click vào để xem chi tiết

3. **Kiểm tra từng job**:
   - ✅ `detect-changes`: Phát hiện service nào thay đổi
   - ✅ `build-and-push`: Build và push images
   - ✅ `deploy`: Deploy lên VPS

### Bước 3.2: Kiểm tra trên VPS sau khi deploy

```bash
# SSH vào VPS
ssh YOUR_USER@YOUR_VPS_IP

cd /opt/pbl6-backend

# Xem các container đang chạy
docker compose ps

# Output mong muốn (sau khi deploy thành công):
# NAME                   STATUS          PORTS
# api-gateway            Up (healthy)    0.0.0.0:8080->8080/tcp
# auth-service           Up (healthy)    0.0.0.0:8081->8081/tcp
# auth-postgres-db       Up (healthy)    0.0.0.0:5432->5432/tcp
# redis-jwt-blacklist    Up (healthy)    0.0.0.0:6379->6379/tcp

# Xem logs
docker compose logs -f --tail=100

# Test health check
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

### Bước 3.3: Test API từ bên ngoài

```bash
# Từ máy local, test API
curl http://YOUR_VPS_IP:8080/actuator/health

# Nếu có domain
curl http://yourdomain.com:8080/actuator/health
```

### Bước 3.4: Xem images đã được push lên Registry

1. Vào: https://github.com/vulinhnopro2704?tab=packages
2. Sẽ thấy các packages:
   - `pbl6-quickdn-be/auth-service`
   - `pbl6-quickdn-be/gateway`
   - `pbl6-quickdn-be/order-service`
   - `pbl6-quickdn-be/goongmap-service`

---

## 🎯 CHECKLIST HOÀN THÀNH

### GitHub Setup ✅
- [ ] Tạo GitHub Personal Access Token
- [ ] Thêm 7 secrets vào repository
- [ ] Verify secrets đã được thêm

### VPS Setup ✅
- [ ] SSH vào VPS thành công
- [ ] Tạo SSH key cho GitHub Actions
- [ ] Cài Docker và Docker Compose
- [ ] Tạo thư mục `/opt/pbl6-backend`
- [ ] Tạo file `docker-compose.yml`
- [ ] Tạo file `.env` với các giá trị thực
- [ ] Login GitHub Container Registry
- [ ] Cấu hình firewall (UFW)

### Testing ✅
- [ ] Push code lên GitHub
- [ ] GitHub Actions chạy thành công
- [ ] Images được push lên Registry
- [ ] Services chạy trên VPS
- [ ] Health checks pass
- [ ] API accessible từ bên ngoài

---

## 🔧 TROUBLESHOOTING

### Lỗi: "Permission denied (publickey)"

```bash
# Kiểm tra SSH key
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh

# Test SSH từ local
ssh -i ~/.ssh/github_actions_key YOUR_USER@YOUR_VPS_IP
```

### Lỗi: "denied: permission_denied: write_package"

- Kiểm tra GitHub token có quyền `write:packages`
- Tạo token mới và update secret `REGISTRY_TOKEN`

### Lỗi: "docker: command not found" trên VPS

```bash
# Verify Docker installed
docker --version

# Nếu không có, cài lại
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

### Services không start

```bash
# Check logs
docker compose logs service-name

# Restart
docker compose restart service-name

# Recreate
docker compose up -d --force-recreate service-name
```

### Database connection failed

```bash
# Check database running
docker compose ps | grep db

# Check env vars
cat .env

# Test connection
docker exec -it auth-postgres-db psql -U auth_user -d auth_db
```

---

## 📝 NOTES QUAN TRỌNG

1. **Bảo mật**:
   - Không commit file `.env` vào git
   - Sử dụng mật khẩu mạnh cho database
   - Rotate SSH keys định kỳ

2. **Backup**:
   - Backup database volumes thường xuyên
   - Backup file `.env` an toàn

3. **Monitoring**:
   - Check logs thường xuyên: `docker compose logs -f`
   - Monitor disk space: `df -h`
   - Monitor memory: `free -h`

4. **Updates**:
   - Khi có code mới, chỉ cần push lên GitHub
   - GitHub Actions tự động build và deploy
   - Chỉ services có thay đổi mới được build lại

---

## 🚀 NEXT STEPS

Sau khi setup xong, bạn có thể:

1. **Setup Nginx reverse proxy** để có HTTPS
2. **Setup domain** và SSL certificate (Let's Encrypt)
3. **Setup monitoring** (Prometheus + Grafana)
4. **Setup logging** (ELK Stack hoặc Loki)
5. **Setup backup automation** cho database

---

## 💬 SUPPORT

Nếu gặp vấn đề:
1. Check GitHub Actions logs
2. Check VPS docker logs: `docker compose logs`
3. Verify tất cả secrets đã đúng
4. Ensure VPS có đủ resources (RAM, disk)

**Happy Deploying! 🎉**
