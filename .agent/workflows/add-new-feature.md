---
description: Workflow for adding new features to EV-Go project
---

# Workflow: Add New Feature

Step-by-step guide to add a new feature following Spring Modulith patterns.

---

## 1. Analyze Requirements

- [ ] Identify which module the feature belongs to
- [ ] List entities to create/modify
- [ ] Define API endpoints needed
- [ ] Check dependencies with other modules

---

## 2. Create Entity (internal/)

```java
// src/main/java/com/project/evgo/{module}/internal/{Name}.java
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

## 3. Create Repository (internal/)

```java
// src/main/java/com/project/evgo/{module}/internal/{Name}Repository.java
public interface SomeNameRepository extends JpaRepository<SomeName, Long> {
    
    Optional<SomeName> findByName(String name);
    
    boolean existsByName(String name);
}
```

---

## 4. Create DTOs (module root - public)

```java
// Request DTO
// src/main/java/com/project/evgo/{module}/Create{Name}Request.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSomeNameRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
}

// Response DTO
// src/main/java/com/project/evgo/{module}/{Name}Response.java
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

## 5. Create Service Interface (module root - public)

```java
// src/main/java/com/project/evgo/{module}/{Name}Service.java
public interface SomeNameService {
    
    SomeNameResponse create(CreateSomeNameRequest request);
    
    SomeNameResponse findById(Long id);
    
    List<SomeNameResponse> findAll();
    
    void deleteById(Long id);
}
```

---

## 6. Create Service Implementation (internal/)

```java
// src/main/java/com/project/evgo/{module}/internal/{Name}ServiceImpl.java
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

## 7. Create Controller (internal/web/)

```java
// src/main/java/com/project/evgo/{module}/internal/web/{Name}Controller.java
@RestController
@RequestMapping("/api/v1/some-names")
@RequiredArgsConstructor
@Tag(name = "SomeNames")
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

## 8. Add Error Codes (if needed)

```java
// Add to sharedkernel/ErrorCode.java
SOME_NAME_NOT_FOUND(404, HttpStatus.NOT_FOUND, "SomeName not found"),
SOME_NAME_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "SomeName already exists"),
```

---

## 9. Testing Checklist

- [ ] Run module verification: `mvn test -Dtest=ApplicationModulesTests`
- [ ] Test API via Swagger UI: `http://localhost:8080/swagger-ui.html`
- [ ] Verify CRUD operations
- [ ] Test error handling
- [ ] Test validation

---

## 10. Final Review

- [ ] Code follows coding-style.md
- [ ] Public API in module root, internal code in internal/
- [ ] Error handling uses AppException
- [ ] Response format follows api-design.md
