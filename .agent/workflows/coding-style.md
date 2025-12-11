---
description: Coding conventions and style guide for EV-Go project
---

# Coding Style Guide

This guide follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with Spring Boot conventions.

---

## Language

- **All code, comments, variable names, class names:** English
- **Commit messages:** English

---

## Naming Conventions

### Packages

```java
// Root package
com.project.evgo

// Module packages (lowercase, singular)
com.project.evgo.user
com.project.evgo.station
com.project.evgo.sharedkernel

// Internal packages
com.project.evgo.{module}.internal
com.project.evgo.{module}.internal.web
```

### Classes

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `{Name}` or `{Name}Entity` | `User`, `Station` |
| Repository | `{Name}Repository` | `UserRepository` |
| Service Interface | `{Name}Service` | `UserService` |
| Service Implementation | `{Name}ServiceImpl` | `UserServiceImpl` |
| Controller | `{Name}Controller` | `UserController` |
| Request DTO | `{Action}{Name}Request` | `CreateUserRequest`, `LoginRequest` |
| Response DTO | `{Name}Response` | `UserResponse` |
| Enum | Descriptive name | `UserStatus`, `ErrorCode` |
| Exception | `{Name}Exception` | `AppException` |
| Configuration | `{Name}Config` | `SecurityConfig` |

### Methods

```java
// Service methods: verb + noun
public UserResponse createUser(CreateUserRequest request);
public UserResponse findById(Long id);
public List<UserResponse> findAll();
public UserResponse update(Long id, UpdateUserRequest request);
public void deleteById(Long id);

// Boolean methods: is/has/can/exists prefix
public boolean isActive();
public boolean hasRole(String role);
public boolean existsByEmail(String email);
```

### Variables

```java
// camelCase for variables
private String fullName;
private LocalDateTime createdAt;
private List<Role> roles;

// UPPER_SNAKE_CASE for constants
public static final String JWT_SECRET_KEY = "jwt.secret";
public static final int MAX_RETRY_COUNT = 3;
```

---

## Lombok Usage

### Entity Classes

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### DTOs

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
}
```

### Services

```java
@Service
@RequiredArgsConstructor  // Constructor injection
@Slf4j                    // Logging
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

### Lombok Summary

| Annotation | Use Case |
|------------|----------|
| `@Getter` / `@Setter` | Entity classes |
| `@Data` | DTOs (includes Getter, Setter, ToString, EqualsAndHashCode) |
| `@Builder` | DTOs with builder pattern |
| `@NoArgsConstructor` | JPA entities, DTOs |
| `@AllArgsConstructor` | JPA entities, DTOs |
| `@RequiredArgsConstructor` | Services with constructor injection |
| `@Slf4j` | Classes that need logging |

---

## Entity Pattern

```java
package com.project.evgo.{module}.internal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "table_name")  // plural, snake_case
public class SomeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SomeStatus status = SomeStatus.ACTIVE;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Relationships

```java
// One-to-Many
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders = new ArrayList<>();

// Many-to-One
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

// Many-to-Many
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

---

## Import Ordering

```java
// 1. Java standard
import java.time.LocalDateTime;
import java.util.List;

// 2. Jakarta
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// 3. Spring
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

// 4. Third-party
import lombok.*;
import io.swagger.v3.oas.annotations.*;

// 5. Project
import com.project.evgo.sharedkernel.*;
import com.project.evgo.user.*;
```

---

## Validation

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;
}
```
