# Tài liệu Walkthrough - User Module

Module quản lý người dùng là module lớn nhất trong hệ thống EV-Go, bao gồm các chức năng xác thực, quản lý hồ sơ, quản lý tài khoản (Admin) và quản lý phương tiện.

---

## Tổng quan Module

| Thuộc tính | Giá trị |
|------------|---------|
| **Package** | `com.project.evgo.user` |
| **Display Name** | User Management |
| **Số Services** | 8 (AuthService, UserService, AccountManagementService, VehicleService, AdminReviewService, StationOwnerService, FileRegistrationService, PdfParsingService) |
| **Số Controllers** | 6 (AuthController, UserController, AdminController, VehicleController, VehicleCatalogController, GuestController) |

---

## Mô hình dữ liệu

```mermaid
erDiagram
    USER ||--o{ VEHICLE : owns
    USER ||--o| STATION_OWNER_PROFILE : has
    USER }o--o{ ROLE : has

    USER {
        Long id
        String email
        String phoneNumber
        UserStatus status
        AuthProvider authProvider
        Boolean emailVerified
        Boolean phoneVerified
    }

    VEHICLE {
        Long id
        String brand
        String modelName
        Set connectorTypes
        Boolean inUse
    }

    STATION_OWNER_PROFILE {
        Long id
        String registrationCode
        StationOwnerType ownerType
        StationOwnerStatus status
    }
```

---

## 1. Authentication (AuthService)

### Mô tả
Cung cấp các chức năng xác thực người dùng bao gồm đăng ký, đăng nhập, xác minh email/phone, quên mật khẩu và đăng nhập Google OAuth.

### API Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản mới | ❌ |
| `POST` | `/api/v1/auth/login` | Đăng nhập | ❌ |
| `POST` | `/api/v1/auth/refresh` | Làm mới access token | ❌ |
| `POST` | `/api/v1/auth/logout` | Đăng xuất | ✅ |
| `POST` | `/api/v1/auth/verify-email` | Xác minh email | ❌ |
| `POST` | `/api/v1/auth/verify-phone` | Xác minh số điện thoại | ❌ |
| `POST` | `/api/v1/auth/resend-verification` | Gửi lại mã xác minh | ❌ |
| `POST` | `/api/v1/auth/forgot-password` | Yêu cầu đặt lại mật khẩu | ❌ |
| `POST` | `/api/v1/auth/reset-password` | Đặt lại mật khẩu với token | ❌ |
| `POST` | `/api/v1/auth/google` | Đăng nhập bằng Google | ❌ |
| `POST` | `/api/v1/auth/station-owner/register` | Đăng ký Station Owner (upload PDF) | ❌ |

### Luồng xử lý chính

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthService
    participant EmailService
    participant Redis
    participant DB

    User->>AuthController: POST /register
    AuthController->>AuthService: register(request)
    AuthService->>DB: Check email exists
    AuthService->>DB: Save user (INACTIVE status)
    AuthService->>EmailService: Send verification email
    AuthService->>Redis: Store verification token
    AuthController-->>User: 200 OK (Check email)

    User->>AuthController: POST /verify-email
    AuthController->>AuthService: verifyEmail(token)
    AuthService->>Redis: Validate token
    AuthService->>DB: Update status to ACTIVE + emailVerified=true
    AuthController-->>User: 200 OK (Verified)

    User->>AuthController: POST /login
    AuthController->>AuthService: login(email, password)
    AuthService->>DB: Validate credentials
    AuthService->>AuthService: Generate JWT tokens
    AuthController-->>User: 200 OK (tokens + cookies)
```

### Các tính năng đã implement

- ✅ Đăng ký với email/phone
- ✅ Xác minh email qua token
- ✅ Xác minh phone qua OTP (mock)
- ✅ Đăng nhập với email/phone + password
- ✅ JWT token với refresh token
- ✅ Token blacklist (via Redis)
- ✅ Google OAuth login
- ✅ Quên/đặt lại mật khẩu
- ✅ Station Owner registration với PDF upload

### Files liên quan

| File | Vai trò |
|------|---------|
| `AuthService.java` | Service interface (public) |
| `internal/AuthServiceImpl.java` | Implementation |
| `internal/web/AuthController.java` | REST Controller |
| `security/JwtTokenProvider.java` | JWT token generation/validation |
| `security/JwtAuthenticationFilter.java` | Authentication filter |
| `internal/token/TokenBlacklistService.java` | Token blacklist management |

---

## 2. User Profile (UserService)

### Mô tả
Quản lý thông tin hồ sơ người dùng, bao gồm cập nhật profile, đổi mật khẩu, và quản lý avatar.

### API Endpoints

| Method | Endpoint | Mô tả | Auth | Role |
|--------|----------|-------|------|------|
| `GET` | `/api/v1/users/me` | Lấy thông tin profile hiện tại | ✅ | Any |
| `PUT` | `/api/v1/users/me` | Cập nhật profile | ✅ | Any |
| `PUT` | `/api/v1/users/me/password` | Đổi mật khẩu | ✅ | Any |
| `POST` | `/api/v1/users/me/avatar/upload-signature` | Lấy Cloudinary signature | ✅ | Any |
| `POST` | `/api/v1/users/me/avatar` | Cập nhật avatar URL | ✅ | Any |
| `GET` | `/api/v1/users/me/business-profile` | Lấy business profile | ✅ | STATION_OWNER |
| `PUT` | `/api/v1/users/me/business-profile` | Cập nhật business profile | ✅ | STATION_OWNER |

### Luồng upload Avatar (Cloudinary)

```mermaid
sequenceDiagram
    participant Mobile
    participant Backend
    participant Cloudinary

    Mobile->>Backend: POST /avatar/upload-signature
    Backend-->>Mobile: {signature, timestamp, apiKey, cloudName}
    Mobile->>Cloudinary: Upload image with signature
    Cloudinary-->>Mobile: {url, publicId}
    Mobile->>Backend: POST /avatar {avatarUrl, publicId}
    Backend->>Backend: Update user avatar
    Backend-->>Mobile: 200 OK (UserResponse)
```

### Các tính năng đã implement

- ✅ Xem thông tin profile
- ✅ Cập nhật thông tin cơ bản (fullName, phone, gender, birthday)
- ✅ Đổi mật khẩu (yêu cầu mật khẩu cũ)
- ✅ Upload avatar qua Cloudinary (signed upload)
- ✅ Business profile cho Station Owner

---

## 3. Account Management (AccountManagementService)

### Mô tả
Chức năng dành cho Super Admin để quản lý tất cả tài khoản trong hệ thống.

### API Endpoints

| Method | Endpoint | Mô tả | Auth | Role |
|--------|----------|-------|------|------|
| `GET` | `/api/v1/admin/accounts` | Danh sách tài khoản (paginated) | ✅ | SUPER_ADMIN |
| `GET` | `/api/v1/admin/accounts/{userId}` | Chi tiết tài khoản | ✅ | SUPER_ADMIN |
| `POST` | `/api/v1/admin/accounts/{userId}/lock` | Khóa tài khoản | ✅ | SUPER_ADMIN |
| `POST` | `/api/v1/admin/accounts/{userId}/unlock` | Mở khóa tài khoản | ✅ | SUPER_ADMIN |
| `DELETE` | `/api/v1/admin/accounts/{userId}` | Xóa tài khoản (soft delete) | ✅ | SUPER_ADMIN |

### Các tính năng đã implement

- ✅ Danh sách tài khoản với filter (status, role, search)
- ✅ Pagination và sorting
- ✅ Chi tiết tài khoản
- ✅ Khóa tài khoản (BLOCKED status)
- ✅ Mở khóa tài khoản (ACTIVE status)
- ✅ Soft delete (DELETED status)

---

## 4. Station Owner Review (AdminReviewService)

### Mô tả
Quy trình duyệt đơn đăng ký Station Owner bởi Admin.

### API Endpoints

| Method | Endpoint | Mô tả | Auth | Role |
|--------|----------|-------|------|------|
| `GET` | `/api/v1/admin/station-owner` | Danh sách đơn đăng ký | ✅ | SUPER_ADMIN |
| `GET` | `/api/v1/admin/station-owner/{profileId}` | Chi tiết đơn đăng ký | ✅ | SUPER_ADMIN |
| `PUT` | `/api/v1/admin/station-owner/{profileId}` | Đánh dấu đang xem xét | ✅ | SUPER_ADMIN |
| `POST` | `/api/v1/admin/station-owner/{profileId}/approve` | Duyệt đơn | ✅ | SUPER_ADMIN |
| `POST` | `/api/v1/admin/station-owner/{profileId}/reject` | Từ chối đơn | ✅ | SUPER_ADMIN |

### Luồng duyệt đơn

```mermaid
stateDiagram-v2
    [*] --> SUBMITTED: Submit registration
    SUBMITTED --> UNDER_REVIEW: Admin views
    UNDER_REVIEW --> APPROVED: Admin approves
    UNDER_REVIEW --> REJECTED: Admin rejects
    APPROVED --> [*]: Email notification sent
    REJECTED --> [*]: Email with reason sent
```

### Các tính năng đã implement

- ✅ Danh sách đơn đăng ký với filter theo status
- ✅ Chi tiết đơn đăng ký (bao gồm thông tin từ PDF)
- ✅ Duyệt đơn (gán role STATION_OWNER, gửi email)
- ✅ Từ chối đơn với lý do
- ✅ PDF parsing để trích xuất thông tin

---

## 5. Vehicle Management (VehicleService)

### Mô tả
Quản lý phương tiện của người dùng, hỗ trợ tìm kiếm trạm sạc phù hợp.

### API Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/users/me/vehicles` | Thêm phương tiện mới | ✅ |
| `GET` | `/api/v1/users/me/vehicles` | Danh sách phương tiện | ✅ |
| `GET` | `/api/v1/users/me/vehicles/{id}` | Chi tiết phương tiện | ✅ |
| `PUT` | `/api/v1/users/me/vehicles/{id}` | Cập nhật phương tiện | ✅ |
| `DELETE` | `/api/v1/users/me/vehicles/{id}` | Xóa phương tiện | ✅ |
| `PUT` | `/api/v1/users/me/vehicles/{id}/in-use` | Đặt làm xe đang sử dụng | ✅ |
| `GET` | `/api/v1/users/me/vehicles/in-use` | Lấy xe đang sử dụng | ✅ |

### Các tính năng đã implement

- ✅ CRUD phương tiện
- ✅ Đánh dấu xe đang sử dụng (để tìm trạm sạc phù hợp)
- ✅ Thông tin xe: brand, modelName, loại connector (hỗ trợ nhiều loại)

---

## 6. Station Owner Tracking (StationOwnerService)

### Mô tả
Cho phép người đăng ký Station Owner theo dõi trạng thái đơn của mình.

### API Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/v1/guest/track-registration` | Kiểm tra trạng thái đơn | ❌ |

### Các tính năng đã implement

- ✅ Tra cứu trạng thái đơn đăng ký bằng email + mã số đăng ký

---

## Entities

### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    Long id;
    String email;
    String phoneNumber;
    String password;
    String fullName;
    UserGender gender;
    LocalDate birthday;
    String avatarUrl;
    String avatarPublicId;
    boolean emailVerified = false;
    boolean phoneVerified = false;
    UserStatus status = UserStatus.INACTIVE;  // Default: INACTIVE
    AuthProvider authProvider = AuthProvider.LOCAL;
    String providerId;  // Google ID khi dùng OAuth
    Set<Role> roles;
    Instant passwordChangedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

> [!NOTE]
> **Ý nghĩa các trường đặc biệt:**
> - `status`: Trạng thái tài khoản. Default = `INACTIVE` (chưa xác minh email)
> - `emailVerified`/`phoneVerified`: Cờ xác minh riêng cho email và phone
> - `authProvider`: Phương thức đăng nhập. `LOCAL` = email/password, `GOOGLE` = OAuth
> - `providerId`: ID từ nhà cung cấp OAuth (Google user ID)
> - `passwordChangedAt`: Thời điểm đổi password, dùng để invalidate old tokens

### Vehicle Entity

```java
@Entity
@Table(name = "vehicles")
public class Vehicle {
    Long id;
    Long userId;
    String brand;           // Hãng xe (VD: Tesla, VinFast)
    String modelName;        // Tên model (VD: Model 3, VF8)
    Set<ConnectorType> connectorTypes;  // Hỗ trợ nhiều loại connector
    Boolean inUse = false;
}
```

> [!NOTE]
> **Ý nghĩa các trường đặc biệt:**
> - `connectorTypes`: **Set (không phải single value)** - một xe có thể hỗ trợ nhiều loại đầu sạc
> - `inUse`: Cờ đánh dấu xe đang sử dụng chính. Dùng để tự động lọc trạm sạc phù hợp
> - ⚠️ Các field `licensePlate`, `batteryCapacity`, `createdAt`, `updatedAt` đã bị comment out trong code

### StationOwnerProfile Entity

```java
@Entity
@Table(name = "station_owner_profiles")
public class StationOwnerProfile {
    Long id;
    String registrationCode;  // Mã đăng ký unique
    User user;
    StationOwnerType ownerType;  // INDIVIDUAL hoặc COMPANY
    
    // INDIVIDUAL fields
    String fullName;
    String idNumber;  // CMND/CCCD
    
    // COMPANY fields
    String businessName;
    String taxCode;
    
    // Common fields
    String contactEmail;
    String contactPhone;
    StationOwnerStatus status = StationOwnerStatus.SUBMITTED;
    String bankAccount;
    String bankName;
    String rejectionReason;
    String pdfFilePath;
    String pdfPublicId;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
}
```

> [!NOTE]
> **Ý nghĩa các trường đặc biệt:**
> - `registrationCode`: Mã đăng ký unique, dùng để tra cứu trạng thái
> - `ownerType`: `INDIVIDUAL` = cá nhân, `COMPANY` = doanh nghiệp
> - `status`: Default = `SUBMITTED` (không phải PENDING như docs cũ ghi)
> - `pdfFilePath`/`pdfPublicId`: Đường dẫn và ID của file PDF trên Cloudinary

---

## Enums

### UserStatus

| Status | Mô tả | Cho phép Login |
|--------|-------|----------------|
| `ACTIVE` | Đã xác minh, đang hoạt động | ✅ Có |
| `INACTIVE` | Chưa xác minh email (default) | ❌ Không |
| `BLOCKED` | Bị Admin khóa | ❌ Không |
| `DELETED` | Đã xóa (soft delete) | ❌ Không |

> [!IMPORTANT]
> Không có status `PENDING`. User mới đăng ký có status = `INACTIVE`.

### StationOwnerStatus

| Status | Mô tả | Bước tiếp theo |
|--------|-------|----------------|
| `SUBMITTED` | Mới nộp đơn (default) | Admin mở xem |
| `UNDER_REVIEW` | Admin đang xem xét | Admin quyết định |
| `APPROVED` | Được duyệt | Gán role STATION_OWNER |
| `REJECTED` | Bị từ chối | User nộp lại hoặc liên hệ support |

> [!IMPORTANT]
> Status mặc định là `SUBMITTED` (không phải `PENDING` như docs cũ ghi).

### AuthProvider

| Value | Mô tả |
|-------|-------|
| `LOCAL` | Đăng ký bằng email/password |
| `GOOGLE` | Đăng nhập bằng Google OAuth |

---

## Dependencies

Module `user` phụ thuộc vào:
- `sharedkernel` - DTOs, Enums, Exceptions
- `notification` - Gửi email xác minh, thông báo

Module `user` expose public subpackage:
- `user::security` - JwtTokenProvider, JwtAuthenticationFilter (cho module config sử dụng)

---

## Lưu ý quan trọng

1. **User Status Default**: User mới = `INACTIVE`, sau khi verify email mới thành `ACTIVE`.

2. **Vehicle ConnectorTypes**: Là `Set<ConnectorType>`, không phải single value. Một xe có thể hỗ trợ nhiều loại đầu sạc.

3. **StationOwnerProfile**: Phân biệt INDIVIDUAL và COMPANY với các field khác nhau.
