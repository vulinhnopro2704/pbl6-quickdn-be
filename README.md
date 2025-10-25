## PBL6-QUICKDN-BACKEND

Tài liệu ngắn mô tả tech stack, các service, cách build/run và cách dùng biến môi trường (.env + application.yml).

## Tech stack

- Java JDK 21
- Maven (mvn / mvnw)
- Spring Boot (web, config, etc.)
- Docker & Docker Compose
- (Optional) Goong Map API integration (goongmap-service)

## Các service trong repository

- `auth-service` — xử lý xác thực, quản lý user/token.
- `gateway` — API Gateway / reverse proxy cho các service nội bộ.
- `goongmap-service` — tích hợp với Goong Map API (bản đồ, geocoding, routing).
- `order-service` — xử lý nghiệp vụ đơn hàng.

Mỗi service là một ứng dụng Spring Boot độc lập (maven project). Thư mục tương ứng chứa `pom.xml` và wrapper (`mvnw`, `mvnw.cmd`).

## Build (Maven) — bỏ qua test

Gợi ý chạy trên Windows PowerShell (repo đang chứa `mvnw.cmd` cho Windows):

- Build một service (ví dụ `auth-service`):

```powershell
cd auth-service
.\mvnw.cmd -DskipTests package
```

- Build `gateway`:

```powershell
cd gateway
.\mvnw.cmd -DskipTests package
```

- Nếu bạn có Maven cài sẵn, thay thế `./mvnw.cmd` bằng `mvn`:

```powershell
mvn -DskipTests package
```

Lưu ý: `-DskipTests` sẽ chạy gói (package) mà bỏ qua các test. Nếu bạn muốn hoàn toàn skip cả surefire/failsafe phases, vẫn dùng cờ này là đủ cho hầu hết project.

## Run

1) Chạy jar trực tiếp

- Sau khi build, vào thư mục service và chạy jar trong `target`:

```powershell
cd auth-service
#$env:MY_VAR = "value"    # (tuỳ biến môi trường nếu cần)
java -jar target\*.jar
```

Gợi ý: dùng `target\*.jar` để phù hợp với tên artifact (snapshot/versioned). Nếu bạn biết file tên cụ thể, dùng tên đầy đủ.

2) Chạy bằng Spring Boot wrapper (dev)

```powershell
cd auth-service
.\mvnw.cmd spring-boot:run -DskipTests
```

3) Chạy toàn bộ bằng Docker Compose

```powershell
# ở thư mục gốc repo
# Docker Desktop phải chạy trước
docker-compose up --build
```

Docker Compose đọc file `docker-compose.yml` ở repo gốc. Bạn có thể đặt biến môi trường dùng bởi compose trong file `.env` (xem phần tiếp theo).

## Biến môi trường: .env và `application.yml`

1) .env (dùng cho Docker Compose)

- Docker Compose tự động đọc file `.env` ở cùng thư mục với `docker-compose.yml`. Nội dung ví dụ `.env`:

```text
# .env (ví dụ)
POSTGRES_USER=quickdn
POSTGRES_PASSWORD=secret
GOONG_API_KEY=your_goong_api_key_here
```

- Trong `docker-compose.yml` bạn có thể tham chiếu: `${GOONG_API_KEY}` hoặc truyền trực tiếp vào container qua `environment:`.

2) `application.yml` (Spring Boot) — cách tham chiếu biến môi trường

- Spring Boot hỗ trợ ghi giá trị từ biến môi trường vào `application.yml` bằng cú pháp `${...}`. Ví dụ:

```yaml
myapp:
	goong:
		api-key: ${GOONG_API_KEY:}
spring:
	datasource:
		url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/quickdn}
		username: ${SPRING_DATASOURCE_USERNAME:quickdn}
		password: ${SPRING_DATASOURCE_PASSWORD:secret}
```

- Ở ví dụ trên, `GOONG_API_KEY` sẽ được lấy từ biến môi trường (nếu không có thì để rỗng). Bạn có thể cung cấp giá trị mặc định sau dấu `:`.

3) Tên biến môi trường (Spring Boot)

- Spring Boot hỗ trợ mapping từ properties sang biến môi trường theo quy ước: `spring.datasource.url` -> `SPRING_DATASOURCE_URL` (chuyển dấu chấm thành gạch dưới, in hoa). Tuy nhiên, khi bạn dùng `${ENV_VAR}` trong `application.yml`, thì Spring sẽ lấy trực tiếp biến môi trường `ENV_VAR`.

4) Ví dụ chạy local với biến môi trường trên PowerShell

```powershell
# $env:GOONG_API_KEY = "your_key"
# $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/quickdn"
$env:GOONG_API_KEY = "your_key_here"; java -jar target\*.jar
```

Lưu ý: trên PowerShell bạn đặt biến môi trường cho phiên hiện tại bằng cách gán `$env:NAME = "value"`.

## Tips & Troubleshooting

- Kiểm tra `target` sau khi build để biết chính xác tên jar.
- Nếu thay đổi `application.yml` nhưng Spring Boot không pick up giá trị bạn mong muốn, kiểm tra thứ tự precedence: command-line args > SPRING_APPLICATION_JSON > env vars > application.yml.
- Để bật profile cụ thể khi chạy jar:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"; java -jar target\*.jar
```

- Để build mọi service tự động, bạn có thể viết script PowerShell nhỏ chạy `mvnw.cmd -DskipTests package` cho từng folder.

## Kết luận

README này cung cấp hướng dẫn cơ bản để build và chạy từng service hoặc toàn bộ hệ thống bằng Docker Compose, cùng các ví dụ cách dùng `.env` và cách tham chiếu biến môi trường trong `application.yml`.

Nếu bạn muốn mình thêm: mẫu `.env` cụ thể cho từng môi trường (dev/staging/production), script build nhiều module tự động, hoặc hướng dẫn CI/CD (GitHub Actions / GitLab CI), nói mình biết để mình bổ sung.

