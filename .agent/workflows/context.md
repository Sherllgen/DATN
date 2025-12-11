---
description: Project overview for EV-Go - architecture, tech stack, and directory structure
---

# EV-Go Project Context

## Project Overview

**Name:** EV-Go - Electric Vehicle Charging Station Management System

**Purpose:**
- Mobile app for users to find and book electric motorcycle charging stations
- Web dashboard for station owners to manage their stations
- Admin portal for system administrators

**Target Users:**
- **Super Admin:** Manages entire system, users, revenue, and complaints
- **Station Owner:** Manages charging stations, monitors operations and revenue
- **User:** Searches, books, pays, charges, and reviews stations

---

## Architecture: Modular Monolith with Spring Modulith

This project uses **Modular Monolithic** architecture with Spring Modulith instead of traditional Maven multi-module.

### Key Characteristics
- Single deployable unit (monolith)
- Logical separation into independent modules
- Module boundaries enforced at compile-time and runtime
- `@ApplicationModule` annotation defines module boundaries
- Inter-module communication through well-defined public APIs

---

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Java** | OpenJDK | 21 |
| **Framework** | Spring Boot | 4.0.0 |
| **Modulith** | Spring Modulith | 2.0.0 |
| **Database** | PostgreSQL | Latest |
| **Cache/Session** | Redis | Latest |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Security** | Spring Security + JWT (jjwt 0.11.5) | - |
| **API Docs** | SpringDoc OpenAPI (Swagger) | 2.7.0 |
| **Build Tool** | Maven | - |
| **Code Generation** | Lombok | - |
| **Environment** | dotenv-java | 3.0.0 |

---

## Project Structure

```
evgo/
├── src/main/java/com/project/evgo/
│   ├── EvgoApplication.java           # Application entry point
│   ├── config/                         # Shared configurations
│   │   ├── CorsConfig.java
│   │   ├── OpenApiConfig.java
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   │
│   ├── sharedkernel/                   # Shared Kernel Module
│   │   ├── package-info.java
│   │   ├── ApiResponse.java            # Standard response wrapper
│   │   ├── PageResponse.java           # Pagination response
│   │   ├── ErrorCode.java              # Error codes enum
│   │   └── AppException.java           # Custom exception
│   │
│   ├── user/                           # User Module
│   │   ├── package-info.java
│   │   ├── UserService.java            # PUBLIC: Service interface
│   │   ├── LoginRequest.java           # PUBLIC: Request DTO
│   │   ├── UserResponse.java           # PUBLIC: Response DTO
│   │   └── internal/                   # HIDDEN from other modules
│   │       ├── UserEntity.java
│   │       ├── RoleEntity.java
│   │       ├── UserRepository.java
│   │       ├── UserServiceImpl.java
│   │       └── web/
│   │           └── UserController.java
│   │
│   └── station/                        # Station Module
│       ├── package-info.java
│       ├── StationService.java         # PUBLIC: Service interface
│       ├── StationRequest.java         # PUBLIC: Request DTO
│       ├── StationResponse.java        # PUBLIC: Response DTO
│       └── internal/
│           ├── StationEntity.java
│           ├── StationRepository.java
│           ├── StationServiceImpl.java
│           └── web/
│               └── StationController.java
│
├── src/main/resources/
│   └── application.properties
│
├── pom.xml
└── .env.example
```

---

## Module Structure Pattern

Each module follows this structure:

```
{module}/
├── package-info.java           # Required: @ApplicationModule
├── {Module}Service.java        # PUBLIC: Service interface
├── {DTO}Request.java           # PUBLIC: Request DTOs
├── {DTO}Response.java          # PUBLIC: Response DTOs
└── internal/                   # HIDDEN: Not accessible from other modules
    ├── {Entity}.java           # JPA Entities
    ├── {Entity}Repository.java # Spring Data Repositories
    ├── {Module}ServiceImpl.java
    └── web/
        └── {Module}Controller.java
```

> **Important:** Classes in `internal/` package are NOT accessible from other modules. Spring Modulith enforces this boundary.

---

## Module Dependencies

```
                    ┌─────────────────┐
                    │  sharedkernel   │
                    │  (Shared DTOs,  │
                    │   Exceptions)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │   user   │   │ station  │   │   ...    │
        └──────────┘   └──────────┘   └──────────┘
```

**Rules:**
- All modules can depend on `sharedkernel`
- No circular dependencies allowed
- Inter-module communication via public APIs or Events
