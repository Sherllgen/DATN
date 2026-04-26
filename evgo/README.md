# EV-Go 🔋⚡

> Ứng dụng di động tìm kiếm trạm sạc xe máy điện và website quản lý cho chủ trạm và quản trị hệ thống
>
> A mobile-first Electric Vehicle Charging Station Management System built with Spring Boot 4.0 and Spring Modulith.

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.0](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith 2.0](https://img.shields.io/badge/Spring%20Modulith-2.0.0-green.svg)](https://spring.io/projects/spring-modulith)

## 📖 Overview

EV-Go is an electric vehicle charging station management system designed for electric motorcycles. It provides:

- **Mobile App** - For users (User/Guest) to find, book, and pay for charging sessions
- **Web Dashboard** - For station owners to manage their stations and monitor revenue
- **Admin Portal** - For Super Admin and Staff to manage the entire platform

## 🏗️ Architecture

This project uses a **Modular Monolithic** architecture with Spring Modulith, providing:

- Single deployable unit with clear module boundaries
- Compile-time and runtime boundary enforcement
- Event-driven inter-module communication
- Easy migration path to microservices if needed

### Module Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      sharedkernel                           │
│              (DTOs, Exceptions, Enums)                      │
└─────────────────────────────────────────────────────────────┘
                              ▲
     ┌────────────────────────┼────────────────────────┐
     │                        │                        │
┌────┴────┐  ┌────────────────┴────────────────┐  ┌────┴────┐
│  user   │  │  station   charger   booking   │  │ payment │
└─────────┘  │  charging  review   complaint  │  └─────────┘
             │          notification          │
             └────────────────────────────────┘
```

### Modules

| Module | Description |
|--------|-------------|
| `user` | User authentication, authorization, and profile management |
| `station` | Charging station and location management |
| `charger` | Individual charger and connector management |
| `booking` | Reservation and scheduling system |
| `charging` | Active charging session management |
| `payment` | Payment processing and transaction history |
| `review` | User ratings and feedback |
| `complaint` | Issue tracking and resolution |
| `notification` | Push notifications and alerts |
| `sharedkernel` | Shared DTOs, exceptions, and enums |

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.0 |
| **Modulith** | Spring Modulith 2.0.0 |
| **Database** | PostgreSQL |
| **Cache/Session** | Redis |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT |
| **Payment** | MoMo One-Time Payment API |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Build Tool** | Maven |

## 🚀 Getting Started

### Prerequisites

- Java 21 or later
- Maven 3.9+
- Docker & Docker Compose (for development)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd evgo
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start the services using Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application**
   - API: http://localhost:8081
   - Swagger UI: http://localhost:8081/swagger-ui.html

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/evgo_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing secret (min 256 bits) | - |
| `PORT` | Application port | `8080` |


### Google OAuth Configuration & Testing

To enable Google Login, you need a Google Cloud Project with OAuth 2.0 Credentials.

1.  **Get Client ID**:
    -   Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
    -   Create OAuth 2.0 Client ID (Web Application).
    -   Add `https://developers.google.com/oauthplayground` to **Authorized redirect URIs**.

2.  **Configure App**:
    -   Update `.env`: `GOOGLE_CLIENT_ID=your-client-id-here`.
    -   Restart the application.

3.  **Test with OAuth Playground**:
    -   Go to [OAuth 2.0 Playground](https://developers.google.com/oauthplayground).
    -   Click Gear Icon ⚙️ -> Check "Use your own OAuth credentials" -> Enter your OAuth Client ID (and Client Secret if asked, though backend only needs ID).
    -   Select scopes: `email`, `profile`, `openid`.
    -   Authorize API -> "Exchange authorization code for tokens".
    -   Copy the **id_token** (NOT access_token).

4.  **Verify via API**:
    -   Endpoint: `POST http://localhost:8081/api/v1/auth/google`
    -   Body:
        ```json
        {
          "idToken": "your_id_token_here"
        }
        ```

## 📚 Documentation

### Module Documentation Generator

This project uses **Spring Modulith Documenter** to automatically generate architecture documentation.

#### Generate Documentation

```bash
mvn test -Dtest=DocumenterTests
```

#### Output Location

Generated files are in `target/spring-modulith-docs/`:

| File | Description |
|------|-------------|
| `components.puml` | PlantUML C4 component diagram |
| `module-{name}.puml` | Individual module diagrams |
| `module-{name}.adoc` | Module canvas documentation |
| `all-docs.adoc` | Aggregating document |

#### Viewing PlantUML Diagrams

- **IntelliJ IDEA**: Install "PlantUML Integration" plugin
- **VS Code**: Install "PlantUML" extension
- **Online**: [PlantUML Web Server](http://www.plantuml.com/plantuml/uml)

### API Documentation

Swagger UI is available at `/swagger-ui.html` when the application is running.

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Module Architecture Tests

```bash
./mvnw test -Dtest=ModularityTests
```

### Run Documentation Generation

```bash
./mvnw test -Dtest=DocumenterTests
```

## 📁 Project Structure

```
evgo/
├── src/main/java/com/project/evgo/
│   ├── EvgoApplication.java           # Entry point
│   ├── config/                         # Shared configurations
│   ├── sharedkernel/                   # Shared DTOs, Exceptions
│   ├── user/                           # User module
│   ├── station/                        # Station module
│   ├── charger/                        # Charger module
│   ├── booking/                        # Booking module
│   ├── charging/                       # Charging module
│   ├── payment/                        # Payment module
│   ├── review/                         # Review module
│   ├── complaint/                      # Complaint module
│   └── notification/                   # Notification module
│
├── src/test/java/com/project/evgo/
│   ├── DocumenterTests.java            # Documentation generator
│   └── ModularityTests.java            # Architecture tests
│
├── .agent/workflows/                   # AI assistant context files
├── pom.xml                             # Maven configuration
├── DOCUMENTER.md                       # Documentation guide
└── README.md                           # This file
```

### Module Structure Pattern

Each module follows this structure:

```
{module}/
├── package-info.java           # @ApplicationModule annotation
├── {Module}Service.java        # PUBLIC: Service interface
├── request/                    # PUBLIC: Request DTOs
├── response/                   # PUBLIC: Response DTOs
└── internal/                   # HIDDEN: Module internals
    ├── {Entity}.java           # JPA Entities
    ├── {Entity}Repository.java # Repositories
    ├── {Module}DtoConverter.java
    ├── {Module}ServiceImpl.java
    └── web/
        └── {Module}Controller.java
```

> **Note**: Classes in `internal/` package are NOT accessible from other modules. Spring Modulith enforces this boundary at compile-time.

## 🔒 Security

- JWT-based authentication with access and refresh tokens
- HTTP-only cookies for token storage
- Role-based access control:
  - **Super Admin** - Full system management
  - **Staff (CSS)** - Customer service, complaint handling
  - **Station Owner** - Station and charger management
  - **User** - Booking, charging, payment
  - **Guest** - View-only access, registration
- Redis for session management and token blacklisting
- MoMo e-wallet integration for secure payments

## 📖 Additional Resources

- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot 4.0 Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PlantUML C4 Model](https://github.com/plantuml-stdlib/C4-PlantUML)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
