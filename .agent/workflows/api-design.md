---
description: REST API design guidelines for EV-Go project
---

# API Design Guide

This guide follows REST best practices and OpenAPI standards.

---

## Response Format

### Standard Response Wrapper

All API responses use `ApiResponse<T>`:

```json
{
    "status": 200,
    "message": "Success",
    "data": { ... }
}
```

### Success Examples

```json
// Single item
{
    "status": 200,
    "message": "User retrieved successfully",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "fullName": "John Doe"
    }
}

// List
{
    "status": 200,
    "message": "Success",
    "data": [...]
}

// Paginated
{
    "status": 200,
    "message": "Success",
    "data": {
        "content": [...],
        "page": 0,
        "size": 10,
        "totalElements": 100,
        "totalPages": 10
    }
}

// No content
{
    "status": 200,
    "message": "Deleted successfully",
    "data": null
}
```

---

## Error Handling

### ErrorCode Enum

```java
public enum ErrorCode {
    // Client errors (4xx)
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Resource not found"),
    
    // Business errors
    USER_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "User already exists"),
    INVALID_CREDENTIALS(400, HttpStatus.BAD_REQUEST, "Invalid credentials"),
    
    // Server errors (5xx)
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
}
```

### Throwing Exceptions

```java
public UserResponse findById(Long id) {
    return userRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
}

// With custom message
throw new AppException(ErrorCode.USER_ALREADY_EXISTS, 
    "Email " + email + " is already registered");
```

### Error Response

```json
{
    "status": 404,
    "message": "Resource not found",
    "data": null
}
```

---

## REST Endpoint Conventions

### URL Patterns

```
GET    /api/v1/{resources}           # List all
GET    /api/v1/{resources}/{id}      # Get by ID
POST   /api/v1/{resources}           # Create
PUT    /api/v1/{resources}/{id}      # Full update
PATCH  /api/v1/{resources}/{id}      # Partial update
DELETE /api/v1/{resources}/{id}      # Delete

# Nested resources
GET    /api/v1/stations/{stationId}/chargers
POST   /api/v1/stations/{stationId}/chargers

# Actions
POST   /api/v1/users/{id}/activate
POST   /api/v1/sessions/{id}/complete
```

### Query Parameters

```
# Pagination
GET /api/v1/stations?page=0&size=10&sort=name,asc

# Filtering
GET /api/v1/stations?status=ACTIVE&city=HCM

# Searching
GET /api/v1/stations?q=keyword
```

---

## Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        var result = userService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        var result = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        var result = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Created successfully")
                        .data(result)
                        .build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted successfully")
                .build());
    }
}
```

---

## HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 | GET, PUT, PATCH, DELETE success |
| 201 | POST success (resource created) |
| 204 | DELETE success (no content) |
| 400 | Bad request, validation error |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not found |
| 409 | Conflict |
| 500 | Internal server error |

---

## Request Validation

```java
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

---

## OpenAPI Documentation

```java
@Operation(summary = "Get user by ID")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "404", description = "User not found")
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getById(
        @Parameter(description = "User ID") @PathVariable Long id) {
    // ...
}
```
