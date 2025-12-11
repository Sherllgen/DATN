---
description: Spring Modulith patterns and best practices for EV-Go project
---

# Spring Modulith Patterns

Reference: [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)

---

## Overview

Spring Modulith enables **Modular Monolithic** architecture with:
- Module boundaries enforced at compile-time and runtime
- Clear separation between public API and internal implementation
- Event-driven communication between modules
- Built-in module testing and documentation

---

## Module Structure

### package-info.java (Required)

Every module MUST have `package-info.java` at root package:

```java
@org.springframework.modulith.ApplicationModule(
    displayName = "User Management"
)
package com.project.evgo.user;
```

### Public vs Internal

```
com.project.evgo.user/
├── package-info.java           # Module definition
├── UserService.java            # ✅ PUBLIC: Accessible by other modules
├── CreateUserRequest.java      # ✅ PUBLIC: Request DTO
├── UserResponse.java           # ✅ PUBLIC: Response DTO
└── internal/                   # 🔒 INTERNAL: Hidden from other modules
    ├── User.java               # Entity
    ├── Role.java               # Entity
    ├── UserRepository.java     # Repository
    ├── UserServiceImpl.java    # Service implementation
    └── web/
        └── UserController.java # REST Controller
```

**Key Rules:**
- Types in module root = **Public API** (accessible by other modules)
- Types in `internal/` = **Hidden** (not accessible by other modules)
- Spring Modulith verifies boundaries at test time

---

## Named Interfaces

To expose specific sub-packages:

```java
// In internal/spi/package-info.java
@org.springframework.modulith.NamedInterface("spi")
package com.project.evgo.user.internal.spi;
```

Other modules can reference:
```java
@ApplicationModule(allowedDependencies = "user :: spi")
package com.project.evgo.station;
```

---

## Inter-Module Communication

### Option 1: Direct Service Call

```java
// In station module - calling user module's public API
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {
    
    private final UserService userService;  // ✅ Public interface
    
    public void assignOwner(Long stationId, Long userId) {
        UserResponse user = userService.findById(userId);
        // ...
    }
}
```

### Option 2: Event-Driven (Recommended for Decoupling)

**1. Define Event in publishing module's root package:**
```java
// com.project.evgo.user/UserCreatedEvent.java
public record UserCreatedEvent(Long userId, String email) {}
```

**2. Publish event:**
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final ApplicationEventPublisher events;
    
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // ... save user
        events.publishEvent(new UserCreatedEvent(user.getId(), user.getEmail()));
        return response;
    }
}
```

**3. Listen in another module:**
```java
@Service
public class NotificationServiceImpl implements NotificationService {
    
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        // Send welcome email
    }
}
```

### Option 3: Transactional Event Listener

```java
@ApplicationModuleListener
public void onUserCreated(UserCreatedEvent event) {
    // Runs in same transaction as publisher
}
```

---

## Module Verification

### Verify All Modules

```java
package com.project.evgo;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTests {
    
    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(EvgoApplication.class).verify();
    }
}
```

### Test Single Module

```java
@ApplicationModuleTest
class UserModuleTests {
    
    @Autowired
    UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Test in isolation
    }
}
```

### Generate Documentation

```java
@Test
void generateDocs() throws IOException {
    var modules = ApplicationModules.of(EvgoApplication.class);
    new Documenter(modules)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml();
}
```

---

## Open Modules

For shared code (like `sharedkernel`):

```java
@org.springframework.modulith.ApplicationModule(
    displayName = "Shared Kernel",
    type = ApplicationModule.Type.OPEN
)
package com.project.evgo.sharedkernel;
```

---

## Common Mistakes

### ❌ Accessing Internal Classes

```java
// In station module
import com.project.evgo.user.internal.User;  // ❌ Illegal!
```

### ❌ Circular Dependencies

```java
// user depends on station
// station depends on user
// ❌ Verification will fail
```

**Solution:** Use events for decoupling

### ❌ Missing package-info.java

Module won't be recognized without `package-info.java`

### ❌ Entities in Public Package

```java
// ❌ Wrong: Entity should be internal
package com.project.evgo.user;
public class User { }  // Should be in internal/
```
