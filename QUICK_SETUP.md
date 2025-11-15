# 📝 QUICK SETUP CHECKLIST

## ✅ PHẦN 1: GITHUB (5 phút)

### Tạo GitHub Token
1. ➡️ Vào: https://github.com/settings/tokens
2. ➡️ Click "Generate new token (classic)"
3. ➡️ Chọn quyền: `write:packages` và `repo`
4. ➡️ Copy token (bắt đầu với `ghp_...`)

### Thêm GitHub Secrets
➡️ Vào: https://github.com/vulinhnopro2704/PBL6-QUICKDN-BACKEND/settings/secrets/actions

**Thêm tổng cộng 29 secrets** (xem chi tiết trong file `ENV_VARIABLES.md`):

#### Registry & VPS (6 secrets):
```
REGISTRY_USERNAME = vulinhnopro2704
REGISTRY_TOKEN = ghp_xxxxxxxxxxxx (token vừa tạo)
VPS_HOST = 123.45.67.89 (IP VPS của bạn)
VPS_USERNAME = root (hoặc ubuntu)
VPS_SSH_KEY = (sẽ điền sau khi setup VPS)
VPS_PORT = 22
```

#### Service URLs (3 secrets):
```
AUTH_SERVICE_URL = http://auth-service:8081
GOONGMAP_SERVICE_URL = http://goongmap-service:8082
ORDER_SERVICE_URL = http://order-service:8083
```

#### Database (9 secrets):
```
AUTH_DB_NAME = auth_db
AUTH_DB_USER = auth_user
AUTH_DB_PASSWORD = (tạo mật khẩu mạnh)

GOONGMAP_DB_NAME = goongmap_db
GOONGMAP_DB_USER = goongmap_user
GOONGMAP_DB_PASSWORD = (tạo mật khẩu mạnh, khác auth)

ORDER_DB_NAME = order_db
ORDER_DB_USER = order_user
ORDER_DB_PASSWORD = (tạo mật khẩu mạnh, khác 2 cái trên)
```

#### JWT & API (4 secrets):
```
JWT_SECRET = (64 ký tự random: openssl rand -hex 32)
JWT_ACCESS_EXP_MS = 3600000
JWT_REFRESH_EXP_MS = 86400000
GOONGMAP_API_KEY = (lấy từ https://account.goong.io/)
```

#### CORS (5 secrets):
```
CORS_ALLOWED_ORIGINS = https://yourdomain.com,http://localhost:3000
CORS_ALLOWED_METHODS = GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS = Content-Type,Authorization
CORS_EXPOSED_HEADERS = Authorization
CORS_ALLOW_CREDENTIALS = true
```

#### Application (1 secret):
```
SPRING_PROFILES_ACTIVE = prod
```

💡 **Tip**: Tạo mật khẩu mạnh bằng: `openssl rand -base64 24`

---

## ✅ PHẦN 2: VPS (10 phút)

### SSH vào VPS
```bash
ssh root@YOUR_VPS_IP
```

### Copy & Paste từng khối lệnh:

**1. Update & Install Docker:**
```bash
sudo apt update && sudo apt upgrade -y
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
docker --version
```

**2. Tạo SSH Key:**
```bash
mkdir -p ~/.ssh
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions_key -N ""
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

**3. Copy Private Key (QUAN TRỌNG):**
```bash
cat ~/.ssh/github_actions_key
```
➡️ Copy **toàn bộ** output (từ -----BEGIN đến -----END)
➡️ Paste vào GitHub Secret `VPS_SSH_KEY`

**4. Login GitHub Registry:**
```bash
echo "YOUR_GITHUB_TOKEN" | docker login ghcr.io -u vulinhnopro2704 --password-stdin
```
⚠️ Thay `YOUR_GITHUB_TOKEN` bằng token thật (secret `REGISTRY_TOKEN`)

**5. Setup Firewall:**
```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable
```
⚠️ Nhập `y` khi được hỏi

**XONG! GitHub Actions sẽ tự động tạo:**
- ✅ Folder `~/pbl6-backend`
- ✅ File `.env` từ GitHub secrets
- ✅ File `docker-compose.yml`

---

## ✅ PHẦN 3: TEST (2 phút)

### Trên máy local:

**1. Commit và Push:**
```bash
git add .
git commit -m "Setup CI/CD"
git push origin main
```

**2. Xem GitHub Actions:**
➡️ Vào: https://github.com/vulinhnopro2704/PBL6-QUICKDN-BACKEND/actions
➡️ Chờ workflow chạy xong (khoảng 5-10 phút)

**3. Kiểm tra VPS:**
```bash
ssh root@YOUR_VPS_IP
cd ~/pbl6-backend
docker compose ps
```

Kết quả mong muốn:
```
NAME                 STATUS
api-gateway          Up (healthy)
auth-service         Up (healthy)
auth-postgres-db     Up (healthy)
redis-jwt-blacklist  Up (healthy)
```

**4. Test API:**
```bash
curl http://YOUR_VPS_IP:8080/actuator/health
```

Kết quả mong muốn:
```json
{"status":"UP"}
```

---

## 🎉 DONE!

Từ giờ, mỗi khi bạn push code lên GitHub:
- ✅ GitHub Actions tự động detect service nào thay đổi
- ✅ Build các service đó
- ✅ Đóng gói thành Docker image
- ✅ Push lên GitHub Container Registry
- ✅ Tạo/update file `.env` trên VPS từ secrets
- ✅ Copy `docker-compose.yml` lên VPS
- ✅ SSH vào VPS và update services

**Bạn không cần chạm vào VPS nữa!** Chỉ cần push code lên GitHub! 🚀

---

## 🆘 NẾU GẶP LỖI

### Lỗi 1: "Permission denied (publickey)"
```bash
# Trên VPS
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

### Lỗi 2: "denied: permission_denied"
- Tạo lại GitHub Token với quyền `write:packages`
- Update secret `REGISTRY_TOKEN` trên GitHub

### Lỗi 3: "docker: command not found"
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

### Lỗi 4: Container không start
```bash
cd ~/pbl6-backend
docker compose logs service-name
```

### Lỗi 5: Không connect được database
- Kiểm tra file `.env` có đúng password không
- Kiểm tra database container đang chạy: `docker compose ps`

---

## 📞 CONTACT

Nếu vẫn gặp vấn đề, check:
1. GitHub Actions logs
2. VPS docker logs: `docker compose logs -f`
3. Verify tất cả secrets đã nhập đúng

Good luck! 🚀
