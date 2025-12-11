---
description: Workflow for creating new Spring Modulith modules in EV-Go project
---

# Workflow: Add New Module

Step-by-step guide to create a new module following Spring Modulith patterns.

---

## 1. Create Package Structure

```bash
# Create directories
mkdir -p src/main/java/com/project/evgo/{modulename}/internal/web
```

```
com.project.evgo.{modulename}/
├── package-info.java           # Required
├── {Name}Service.java          # Public service interface
├── Create{Name}Request.java    # Public request DTO
├── {Name}Response.java         # Public response DTO
└── internal/                   # Hidden from other modules
    ├── {Name}.java             # Entity
    ├── {Name}Repository.java   # Repository
    ├── {Name}ServiceImpl.java  # Service implementation
    └── web/
        └── {Name}Controller.java
```

---

## 2. Create package-info.java

```java
// src/main/java/com/project/evgo/{modulename}/package-info.java
@org.springframework.modulith.ApplicationModule(
    displayName = "Module Display Name"
)
package com.project.evgo.{modulename};
```

---

## 3. Create Entity (internal/)

```java
// src/main/java/com/project/evgo/{modulename}/internal/{Name}.java
package com.project.evgo.{modulename}.internal;

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
@Table(name = "table_name")
public class SomeName {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

## 4. Create Repository (internal/)

```java
// src/main/java/com/project/evgo/{modulename}/internal/{Name}Repository.java
package com.project.evgo.{modulename}.internal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SomeNameRepository extends JpaRepository<SomeName, Long> {
}
```

---

## 5. Create Public DTOs (module root)

```java
// src/main/java/com/project/evgo/{modulename}/Create{Name}Request.java
package com.project.evgo.{modulename};

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSomeNameRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
}
```

```java
// src/main/java/com/project/evgo/{modulename}/{Name}Response.java
package com.project.evgo.{modulename};

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SomeNameResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

---

## 6. Create Public Service Interface (module root)

```java
// src/main/java/com/project/evgo/{modulename}/{Name}Service.java
package com.project.evgo.{modulename};

import java.util.List;

public interface SomeNameService {
    
    SomeNameResponse create(CreateSomeNameRequest request);
    
    SomeNameResponse findById(Long id);
    
    List<SomeNameResponse> findAll();
    
    void deleteById(Long id);
}
```

---

## 7. Create Service Implementation (internal/)

```java
// src/main/java/com/project/evgo/{modulename}/internal/{Name}ServiceImpl.java
package com.project.evgo.{modulename}.internal;

import com.project.evgo.{modulename}.*;
import com.project.evgo.sharedkernel.ErrorCode;
import com.project.evgo.sharedkernel.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SomeNameServiceImpl implements SomeNameService {
    
    private final SomeNameRepository repository;
    
    @Override
    public SomeNameResponse create(CreateSomeNameRequest request) {
        var entity = new SomeName();
        entity.setName(request.getName());
        var saved = repository.save(entity);
        return toResponse(saved);
    }
    
    @Override
    public SomeNameResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
    
    @Override
    public List<SomeNameResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
    
    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        repository.deleteById(id);
    }
    
    private SomeNameResponse toResponse(SomeName entity) {
        return SomeNameResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

---

## 8. Create Controller (internal/web/)

```java
// src/main/java/com/project/evgo/{modulename}/internal/web/{Name}Controller.java
package com.project.evgo.{modulename}.internal.web;

import com.project.evgo.{modulename}.*;
import com.project.evgo.sharedkernel.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/some-names")
@RequiredArgsConstructor
@Tag(name = "SomeNames", description = "SomeName management APIs")
public class SomeNameController {
    
    private final SomeNameService service;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SomeNameResponse>>> getAll() {
        var result = service.findAll();
        return ResponseEntity.ok(ApiResponse.<List<SomeNameResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeNameResponse>> getById(@PathVariable Long id) {
        var result = service.findById(id);
        return ResponseEntity.ok(ApiResponse.<SomeNameResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SomeNameResponse>> create(
            @Valid @RequestBody CreateSomeNameRequest request) {
        var result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SomeNameResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Created successfully")
                        .data(result)
                        .build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted successfully")
                .build());
    }
}
```

---

## 9. Verify Module Structure

// turbo
```bash
cd evgo && mvn test -Dtest=ApplicationModulesTests
```

This verifies:
- No circular dependencies
- No illegal access to internal packages
- Module boundaries respected

---

## 10. Final Checklist

- [ ] `package-info.java` created with `@ApplicationModule`
- [ ] Entities in `internal/`
- [ ] Repositories in `internal/`
- [ ] DTOs in module root (public)
- [ ] Service interface in module root (public)
- [ ] Service implementation in `internal/`
- [ ] Controller in `internal/web/`
- [ ] Module verification test passes
- [ ] No imports from other modules' `internal/` packages
