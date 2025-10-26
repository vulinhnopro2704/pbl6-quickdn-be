# Hướng dẫn sử dụng manage.ps1

Script PowerShell để build và quản lý các service trong PBL6-QUICKDN-BACKEND.

## Cú pháp

```powershell
.\manage.ps1 <command> [service-name] [-DetailedLog] [-Quiet]
```

## Parameters

- `<command>` - (Bắt buộc) Command cần thực thi
- `[service-name]` - (Tùy chọn) Tên service, mặc định là 'all'
- `-DetailedLog` - (Tùy chọn) Hiển thị log chi tiết với --info và --stacktrace
- `-Quiet` - (Tùy chọn) Chế độ tối giản, chỉ hiển thị errors

## Commands có sẵn

- `build` - Build service(s)
- `clean` - Xóa build artifacts
- `test` - Chạy tests
- `bootJar` - Tạo executable JAR file

## Services có sẵn

- `gateway` - API Gateway service
- `auth-service` - Authentication service
- `order-service` - Order management service
- `goongmap-service` - Goong Map integration service
- `all` - Tất cả các services (mặc định)

## Ví dụ sử dụng

### Build tất cả các services
```powershell
.\manage.ps1 build
# hoặc
.\manage.ps1 build all
```

### Build một service cụ thể
```powershell
.\manage.ps1 build gateway
.\manage.ps1 build auth-service
.\manage.ps1 build order-service
.\manage.ps1 build goongmap-service
```

### Clean tất cả các services
```powershell
.\manage.ps1 clean all
```

### Clean một service cụ thể
```powershell
.\manage.ps1 clean gateway
```

### Chạy tests cho tất cả services
```powershell
.\manage.ps1 test
```

### Tạo JAR file cho một service
```powershell
.\manage.ps1 bootJar auth-service
```

### Build với log chi tiết (để debug)
```powershell
.\manage.ps1 build gateway -DetailedLog
# hoặc
.\manage.ps1 build all -DetailedLog
```

### Build ở chế độ quiet (chỉ hiển thị errors)
```powershell
.\manage.ps1 build -Quiet
```

### Kết hợp các options
```powershell
# Build một service với detailed log
.\manage.ps1 build auth-service -DetailedLog

# Clean tất cả với quiet mode
.\manage.ps1 clean all -Quiet
```

## Lưu ý

- Script tự động sử dụng `gradlew.bat` của từng service
- Kết quả build sẽ hiển thị màu sắc:
  - 🟢 Xanh lá: Thành công
  - 🔴 Đỏ: Thất bại
  - 🟡 Vàng: Cảnh báo
  - 🔵 Xanh dương: Thông tin
- Script sẽ exit với code 1 nếu có lỗi, code 0 nếu thành công
- **DetailedLog mode**: Thêm `--info` và `--stacktrace` vào Gradle command để xem log chi tiết
- **Quiet mode**: Thêm `--quiet` để chỉ hiển thị warnings và errors

## Troubleshooting

### Lỗi "cannot be loaded because running scripts is disabled"
Chạy PowerShell với quyền Administrator và thực thi:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Lỗi "gradlew.bat không tìm thấy"
Đảm bảo rằng mỗi service đều có file `gradlew.bat` riêng của nó.

### Build bị lỗi
Kiểm tra log chi tiết trong terminal, script sẽ hiển thị output đầy đủ từ Gradle.

## Git Hooks Integration

Bạn có thể tích hợp `manage.ps1` với Git hooks để tự động build/test trước khi push:

### Cài đặt Git Hook

```powershell
# Cài đặt hook đơn giản (build tất cả services)
.\install-hooks.ps1 simple

# Cài đặt hook thông minh (chỉ build services đã thay đổi)
.\install-hooks.ps1 smart

# Cài đặt hook đầy đủ (build + test)
.\install-hooks.ps1 full
```

Xem chi tiết tại [.githooks/README.md](.githooks/README.md)
