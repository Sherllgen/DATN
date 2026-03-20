# Tổng quan Backend Features - EV-Go

Tài liệu này cung cấp cái nhìn tổng quan về các module backend đã được implement trong hệ thống EV-Go.

---

## Module Status Overview

| Module | Status | Mô tả | Tài liệu chi tiết |
|--------|--------|-------|-------------------|
| **User** | ✅ Implemented | Xác thực, quản lý người dùng, tài khoản, phương tiện | [walkthrough-user-module.md](walkthrough-user-module.md) |
| **Station** | ✅ Implemented | Quản lý trạm sạc, ảnh, giá, phê duyệt | [walkthrough-station-module.md](walkthrough-station-module.md) |
| **Charger** | ✅ Implemented | Quản lý trụ sạc và cổng sạc (Có thông tin OCPP) | [walkthrough-charger-module.md](walkthrough-charger-module.md) |
| **OCPP** | ✅ Implemented | Xử lý giao thức OCPP 1.6J (WebSocket) cho trạm sạc | [walkthrough-ocpp-module.md](walkthrough-ocpp-module.md) |
| **Notification** | ✅ Implemented | Email, SMS, thông báo | [walkthrough-notification-module.md](walkthrough-notification-module.md) |
| **Payment** | ✅ Implemented | Thanh toán ZaloPay (App-to-App, Webhooks) | [walkthrough-payment-module.md](walkthrough-payment-module.md) |
| **Shared Kernel** | ✅ Implemented | DTOs, Enums, Exceptions, Infra | [walkthrough-sharedkernel-module.md](walkthrough-sharedkernel-module.md) |
| **Booking** | ✅ Implemented | Đặt lịch sạc, lấy config và metadata | [walkthrough-booking-module.md](walkthrough-booking-module.md) |
| **Charging** | 🔲 Skeleton | Quản lý phiên sạc | *Coming soon* |
| **Review** | ✅ Implemented | Đánh giá trạm sạc | [walkthrough-review-module.md](walkthrough-review-module.md) |
| **Complaint** | 🔲 Skeleton | Khiếu nại | *Coming soon* |

---

## Architecture Overview

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        Mobile["Mobile App (React Native)"]
        Web["Web Admin"]
    end

    subgraph API["API Gateway"]
        Controller["REST Controllers"]
    end

    subgraph Modules["Business Modules"]
        User["User Module"]
        Station["Station Module"]
        Charger["Charger Module"]
        OCPP["OCPP Module"]
        Notification["Notification Module"]
        Payment["Payment Module"]
        Booking["Booking Module"]
        Review["Review Module"]
        Charging["Charging Module 🔲"]
    end

    subgraph Shared["Shared"]
        SK["Shared Kernel"]
        Config["Configuration"]
    end

    subgraph External["External Services"]
        DB[(PostgreSQL)]
        Redis[(Redis)]
        Cloudinary["Cloudinary"]
        Email["SMTP Server"]
        ZaloPay["ZaloPay Gateway"]
    end

    Mobile --> Controller
    Web --> Controller
    Controller --> User
    Controller --> Station
    Controller --> Charger
    Controller --> OCPP
    Controller --> Notification
    Controller --> Payment
    Controller --> Review

    User --> SK
    Station --> SK
    Charger --> SK
    OCPP --> SK
    Notification --> SK
    Payment --> SK
    Review --> SK

    User --> DB
    Station --> DB
    Charger --> DB
    Notification --> DB
    Payment --> DB
    Review --> DB

    User --> Redis
    OCPP --> Redis
    User --> Cloudinary
    Notification --> Email
    Payment --> ZaloPay

    Charger -.->|verifyOwnership| Station
    User -.->|sendEmail| Notification
    OCPP -.->|updateChargerStatus| Charger
```

---

## Module Dependencies

```mermaid
flowchart LR
    SK[Shared Kernel]

    User --> SK
    Station --> SK
    Charger --> SK
    OCPP --> SK
    Notification --> SK
    Payment --> SK
    Review --> SK

    User -->|sendEmail| Notification
    Charger -->|verifyOwnership| Station
    OCPP -->|processBoot| Charger

    style SK fill:#f9f,stroke:#333
    style User fill:#90EE90
    style Station fill:#90EE90
    style Charger fill:#90EE90
    style OCPP fill:#90EE90
    style Notification fill:#90EE90
    style Payment fill:#90EE90
    style Review fill:#90EE90
```

---

## API Endpoints Summary

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/register` | Đăng ký tài khoản |
| POST | `/login` | Đăng nhập |
| POST | `/refresh` | Làm mới token |
| POST | `/logout` | Đăng xuất |
| POST | `/verify-email` | Xác minh email |
| POST | `/verify-phone` | Xác minh phone |
| POST | `/forgot-password` | Quên mật khẩu |
| POST | `/reset-password` | Đặt lại mật khẩu |
| POST | `/google` | Đăng nhập Google |

### User Profile (`/api/v1/users`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/me` | Thông tin profile |
| PUT | `/me` | Cập nhật profile |
| PUT | `/me/password` | Đổi mật khẩu |
| POST | `/me/avatar` | Cập nhật avatar |
| GET | `/me/business-profile` | Business profile (Owner) |
| PUT | `/me/business-profile` | Cập nhật business profile |

### Vehicles (`/api/v1/users/me/vehicles`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/` | Thêm xe |
| GET | `/` | Danh sách xe |
| GET | `/{id}` | Chi tiết xe |
| PUT | `/{id}` | Cập nhật xe |
| DELETE | `/{id}` | Xóa xe |
| PUT | `/{id}/in-use` | Đặt xe đang sử dụng |
| GET | `/in-use` | Lấy xe đang sử dụng |

### Admin (`/api/v1/admin`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/accounts` | Danh sách tài khoản |
| GET | `/accounts/{id}` | Chi tiết tài khoản |
| POST | `/accounts/{id}/lock` | Khóa tài khoản |
| POST | `/accounts/{id}/unlock` | Mở khóa tài khoản |
| DELETE | `/accounts/{id}` | Xóa tài khoản |
| GET | `/station-owner` | Danh sách đơn đăng ký SO |
| GET | `/station-owner/{id}` | Chi tiết đơn đăng ký |
| PUT | `/station-owner/{id}` | Đánh dấu đang xem xét |
| POST | `/station-owner/{id}/approve` | Duyệt đơn |
| POST | `/station-owner/{id}/reject` | Từ chối đơn |

### Stations (`/api/v1/stations`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/` | Danh sách trạm sạc |
| GET | `/{id}` | Chi tiết trạm |
| GET | `/me` | Trạm của tôi (Owner) |
| POST | `/` | Tạo trạm |
| PUT | `/{id}` | Cập nhật trạm |
| DELETE | `/{id}` | Xóa trạm |
| PATCH | `/{id}/status` | Cập nhật trạng thái |

### Chargers (`/api/v1/chargers`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/?stationId={id}` | Danh sách charger theo station |
| GET | `/{id}` | Chi tiết charger |
| POST | `/` | Tạo charger |
| PUT | `/{id}` | Cập nhật charger |
| DELETE | `/{id}` | Xóa charger |
| GET | `/{id}/ports` | Danh sách port |
| POST | `/{id}/ports` | Tạo port |

### Ports (`/api/v1/ports`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/{id}` | Chi tiết port |
| PUT | `/{id}` | Cập nhật trạng thái port |
| DELETE | `/{id}` | Xóa port |

### Payment (`/api/v1/zalopay`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/orders` | Tạo link thanh toán đơn hàng |
| POST | `/callback` | Webhook IPN nhận kết quả |
| GET | `/orders/{appTransId}/status` | Kiểm tra trạng thái đơn hàng |

### OCPP (`/ocpp`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| WS | `/{chargePointId}` | Giao tiếp WebSocket với trụ sạc vật lý |

### Notifications (`/api/v1/notifications`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/{id}` | Chi tiết thông báo |
| GET | `/user/{userId}` | Thông báo của user |
| GET | `/user/{userId}/unread` | Thông báo chưa đọc |
| GET | `/user/{userId}/unread/count` | Đếm chưa đọc |

### Reviews (`/api/v1/reviews`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/station/{id}/summary` | Thống kê đánh giá trạm sạc |
| GET | `/station/{id}` | Danh sách đánh giá (phân trang) |
| POST | `/station/{id}` | Gửi đánh giá mới |
| PUT | `/{id}` | Cập nhật đánh giá (Owner) |
| DELETE | `/{id}` | Xóa đánh giá (Owner/Admin) |

---

## User Roles & Permissions

| Role | Mô tả | Quyền hạn chính |
|------|-------|-----------------|
| **USER** | Người dùng thường | Tìm trạm, đặt lịch, sạc xe, thanh toán |
| **STATION_OWNER** | Chủ trạm sạc | Quản lý trạm, trụ sạc, xem thống kê |
| **SUPER_ADMIN** | Quản trị viên | Quản lý tài khoản, duyệt đơn, thống kê |

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.x |
| Architecture | Spring Modulith |
| Database | PostgreSQL |
| Cache | Redis |
| Security | JWT, Spring Security |
| File Storage | Cloudinary |
| Protocol | HTTP/REST, WebSocket (OCPP 1.6J) |
| Email | SMTP (Gmail) |
| API Docs | OpenAPI 3.0 (Swagger) |
| Build Tool | Maven |

---

## Configuration

### Environment Variables

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/evgo_db
DB_NAME=evgo_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_PORT=5432
#chay local thi de la localhost, chay trong docker thi de postgres
DB_HOST=postgres

#Cloudinary Configuration
CLOUD_NAME=my_cloud_name
CLOUD_API_KEY=my_api_key
CLOUD_API_SECRET=my_api_secret

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=your-secret-key-min-256-bits-long-base64-encoded
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Cookie Configuration
COOKIE_ACCESS_TOKEN_NAME=access_token
COOKIE_ACCESS_TOKEN_MAX_AGE=3600
COOKIE_REFRESH_TOKEN_NAME=refresh_token
COOKIE_REFRESH_TOKEN_MAX_AGE=604800
COOKIE_HTTP_ONLY=true
COOKIE_SECURE=false
COOKIE_SAME_SITE=Lax
COOKIE_PATH=/

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=mail_dung_de_support@gmail.com
MAIL_PASSWORD=your_app_password_here

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com

# File Upload Configuration
APP_UPLOAD_MAX_SIZE=10485760

# Activation Token Configuration
APP_ACTIVATION_TOKEN_EXPIRY_HOURS=24

# Server Configuration
SERVER_PORT=8081

# ZaloPay Sandbox Config
ZALOPAY_APP_ID=2553
ZALOPAY_KEY1=sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn
ZALOPAY_KEY2=trMrHtvjo6myautxDUiAcYsVtaeQ8nhf
ZALOPAY_ENDPOINT=https://sb-openapi.zalopay.vn/v2

# App Callback (dev: use ngrok)
ZALOPAY_CALLBACK_URL=https://abcd-123.ngrok-free.app/api/v1/zalopay/callback
ZALOPAY_REDIRECT_URL=
```

---

## Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Redis 7+
- Maven 3.9+

### Run locally

```bash
# Clone repository
git clone <repository-url>
cd lvtn-251-evgo/evgo

# Set up environment variables
cp .env.example .env
# Edit .env with your values

# Run with Maven
mvn spring-boot:run
```

### API Documentation

Sau khi chạy server, truy cập Swagger UI:
- http://localhost:8081/swagger-ui/index.html

---

## Next Steps (Skeleton Modules)

Các module sau đang ở trạng thái skeleton và sẽ được implement:

1. **Booking Module** - Đặt lịch sạc xe
2. **Charging Module** - Quản lý phiên sạc
