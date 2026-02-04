# Tài liệu Walkthrough - Shared Kernel Module

Module chia sẻ chứa các thành phần dùng chung trong toàn bộ hệ thống: DTOs, Enums, Exceptions, và Infrastructure services.

---

## Tổng quan Module

| Thuộc tính | Giá trị |
|------------|---------|
| **Package** | `com.project.evgo.sharedkernel` |
| **Display Name** | Shared Kernel |
| **Module Type** | OPEN (cho phép tất cả module truy cập) |
| **Mục đích** | Cung cấp shared code cho toàn bộ ứng dụng |

---

## Cấu trúc Module

```
sharedkernel/
├── package-info.java              # @ApplicationModule(type = OPEN)
├── dto/
│   ├── ApiResponse.java           # Standard API response wrapper
│   └── PageResponse.java          # Pagination response wrapper
├── enums/
│   ├── ErrorCode.java             # Centralized error codes
│   ├── UserStatus.java
│   ├── UserGender.java
│   ├── AuthProvider.java
│   ├── StationStatus.java
│   ├── StationOwnerStatus.java
│   ├── StationOwnerType.java
│   ├── ChargerStatus.java
│   ├── ConnectorType.java
│   ├── PortStatus.java
│   ├── BookingStatus.java
│   ├── ChargingSessionStatus.java
│   ├── PaymentStatus.java
│   ├── PaymentMethod.java
│   ├── TransactionStatus.java
│   ├── ComplaintStatus.java
│   ├── NotificationType.java
│   ├── InvoiceStatus.java
│   └── InvoicePurpose.java
├── exceptions/
│   ├── AppException.java          # Custom application exception
│   └── GlobalExceptionHandler.java # Centralized exception handler
└── infra/
    ├── FileStorageService.java    # Cloudinary integration
    └── CloudinaryFileStorageService.java
```

---

## 1. API Response DTOs

### ApiResponse<T>

Wrapper chuẩn cho tất cả API responses:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
}
```

**Ví dụ sử dụng:**

```java
// Success with data
return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
    .status(200)
    .message("User retrieved successfully")
    .data(userResponse)
    .build());

// Success without data (delete, update)
return ResponseEntity.ok(ApiResponse.<Void>builder()
    .status(200)
    .message("Deleted successfully")
    .build());
```

**Response format:**

```json
{
    "status": 200,
    "message": "Success",
    "data": { ... }
}
```

### PageResponse<T>

Wrapper cho paginated responses:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
```

**Ví dụ sử dụng:**

```java
Page<User> userPage = userRepository.findAll(pageable);
PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
    .content(userPage.map(converter::convert).getContent())
    .page(userPage.getNumber())
    .size(userPage.getSize())
    .totalElements(userPage.getTotalElements())
    .totalPages(userPage.getTotalPages())
    .first(userPage.isFirst())
    .last(userPage.isLast())
    .build();
```

---

## 2. Error Handling

### ErrorCode Enum

Centralized error codes với HTTP status mapping:

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "Invalid request"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Access denied"),

    // Authentication errors
    INVALID_CREDENTIALS(400, HttpStatus.BAD_REQUEST, "Invalid email or password"),
    EMAIL_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "Email already registered"),
    PHONE_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "Phone number already registered"),
    USER_NOT_VERIFIED(400, HttpStatus.BAD_REQUEST, "User not verified"),
    USER_BLOCKED(403, HttpStatus.FORBIDDEN, "User account is blocked"),
    INVALID_TOKEN(400, HttpStatus.BAD_REQUEST, "Invalid or expired token"),
    TOKEN_EXPIRED(401, HttpStatus.UNAUTHORIZED, "Token has expired"),

    // User errors
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "User not found"),
    PASSWORD_MISMATCH(400, HttpStatus.BAD_REQUEST, "Current password is incorrect"),

    // Station errors
    STATION_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Station not found"),
    STATION_NOT_OWNED(403, HttpStatus.FORBIDDEN, "You don't own this station"),

    // Charger errors
    CHARGER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Charger not found"),
    PORT_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Port not found"),

    // Vehicle errors
    VEHICLE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Vehicle not found"),
    VEHICLE_NOT_OWNED(403, HttpStatus.FORBIDDEN, "You don't own this vehicle"),

    // Station Owner errors
    REGISTRATION_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Registration not found"),
    INVALID_REGISTRATION_STATUS(400, HttpStatus.BAD_REQUEST, "Invalid registration status");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
```

### AppException

Custom exception class:

```java
@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
```

**Ví dụ sử dụng:**

```java
// Basic usage
throw new AppException(ErrorCode.USER_NOT_FOUND);

// With custom message
throw new AppException(ErrorCode.INVALID_REQUEST, "Email format is invalid");
```

### GlobalExceptionHandler

Centralized exception handling với `@ControllerAdvice`:

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = ex.getCustomMessage() != null
            ? ex.getCustomMessage()
            : errorCode.getMessage();

        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .status(errorCode.getCode())
                .message(message)
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
            .body(ApiResponse.<Void>builder()
                .status(400)
                .message(message)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<Void>builder()
                .status(500)
                .message("Internal server error")
                .build());
    }
}
```

---

## 3. Enums Reference

### User-related Enums

| Enum | Values | Description |
|------|--------|-------------|
| `UserStatus` | PENDING, ACTIVE, BLOCKED, DELETED | Trạng thái tài khoản |
| `UserGender` | MALE, FEMALE, OTHER | Giới tính |
| `AuthProvider` | LOCAL, GOOGLE | Phương thức đăng nhập |

### Station Owner Enums

| Enum | Values | Description |
|------|--------|-------------|
| `StationOwnerStatus` | PENDING, UNDER_REVIEW, APPROVED, REJECTED | Trạng thái đơn đăng ký |
| `StationOwnerType` | INDIVIDUAL, COMPANY | Loại chủ sở hữu |

### Station & Charger Enums

| Enum | Values | Description |
|------|--------|-------------|
| `StationStatus` | OPEN, MAINTENANCE, CLOSED, DELETED | Trạng thái trạm |
| `ChargerStatus` | ACTIVE, INACTIVE, MAINTENANCE | Trạng thái bộ sạc |
| `PortStatus` | AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_ORDER | Trạng thái cổng sạc |
| `ConnectorType` | TYPE_1, TYPE_2, CCS1, CCS2, CHADEMO, TESLA, GB_T | Loại đầu sạc |

### Booking & Charging Enums

| Enum | Values | Description |
|------|--------|-------------|
| `BookingStatus` | PENDING, CONFIRMED, CANCELLED, COMPLETED, EXPIRED | Trạng thái đặt lịch |
| `ChargingSessionStatus` | PENDING, IN_PROGRESS, COMPLETED, CANCELLED, FAILED | Trạng thái phiên sạc |

### Payment Enums

| Enum | Values | Description |
|------|--------|-------------|
| `PaymentStatus` | PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED | Trạng thái thanh toán |
| `PaymentMethod` | MOMO, VNPAY, BANK_TRANSFER, CASH | Phương thức thanh toán |
| `TransactionStatus` | PENDING, SUCCESS, FAILED, CANCELLED | Trạng thái giao dịch |

### Other Enums

| Enum | Values | Description |
|------|--------|-------------|
| `ComplaintStatus` | PENDING, IN_PROGRESS, RESOLVED, REJECTED | Trạng thái khiếu nại |
| `NotificationType` | BOOKING, CHARGING, PAYMENT, SYSTEM, PROMOTION | Loại thông báo |
| `InvoiceStatus` | DRAFT, SENT, PAID, CANCELLED | Trạng thái hóa đơn |
| `InvoicePurpose` | CHARGING, BOOKING_FEE | Mục đích hóa đơn |

---

## 4. Infrastructure Services

### FileStorageService

Interface cho file storage (Cloudinary):

```java
public interface FileStorageService {
    Map<String, String> generateUploadSignature();
    void deleteFile(String publicId);
}
```

### CloudinaryFileStorageService

Implementation với Cloudinary:

```java
@Service
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, String> generateUploadSignature() {
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> params = Map.of(
            "timestamp", timestamp,
            "folder", "evgo/avatars"
        );

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        return Map.of(
            "cloudName", cloudinary.config.cloudName,
            "apiKey", cloudinary.config.apiKey,
            "timestamp", String.valueOf(timestamp),
            "signature", signature,
            "folder", "evgo/avatars"
        );
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (Exception e) {
            log.error("Failed to delete file: {}", publicId, e);
        }
    }
}
```

---

## Dependencies

Module `sharedkernel` là **OPEN module**, được phép truy cập bởi tất cả module khác:

```java
@ApplicationModule(
    displayName = "Shared Kernel",
    type = ApplicationModule.Type.OPEN
)
package com.project.evgo.sharedkernel;
```

Tất cả các module khác đều phụ thuộc vào `sharedkernel`:
- `user`
- `station`
- `charger`
- `notification`
- `booking`
- `charging`
- `payment`
- `review`
- `complaint`

---

## Best Practices

1. **Không thêm business logic vào sharedkernel**: Module này chỉ chứa shared code, không có business logic.

2. **ErrorCode centralization**: Tất cả error codes được định nghĩa tại đây để đảm bảo consistency.

3. **DTO patterns**: Sử dụng `ApiResponse<T>` và `PageResponse<T>` cho tất cả API endpoints.

4. **Enum additions**: Khi thêm enum mới, đảm bảo document ý nghĩa của từng value.
