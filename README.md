# EVGo — Electric Vehicle Charging Station Management System

> **Graduation Project** — Ho Chi Minh City University of Technology (HCMUT)  
> **Team:** EVGo Team

---

[![Spring Boot 4.0.0](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith 2.0.0](https://img.shields.io/badge/Spring%20Modulith-2.0.0-green.svg)](https://spring.io/projects/spring-modulith)
[![Next.js 15](https://img.shields.io/badge/Next.js-15.5-black.svg)](https://nextjs.org/)
[![Expo Go 54](https://img.shields.io/badge/Expo%20Go-54.0-blue.svg)](https://expo.dev/)
[![TailwindCSS v4](https://img.shields.io/badge/TailwindCSS-v4.0-38bdf8.svg)](https://tailwindcss.com/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

## 📖 Project Overview

**EVGo** is a comprehensive management and search system for electric motorcycle charging stations. The platform digitalizes station operations and provides users with a seamless experience for locating stations, booking slots, and making online payments.

The backend leverages a **Modular Monolithic** architecture using Spring Modulith, integrated with a hybrid mobile app (React Native/Expo) for end-users, and a web dashboard (Next.js) for managers and administrators.

---

## 👥 Roles in the System

EVGo supports Role-Based Access Control (RBAC) across five user roles:

1. **Super Admin (Web Dashboard):** Fully controls the system, reviews and approves Station Owner registrations, monitors system-wide revenue, and manages user accounts.
2. **Staff / CSS (Web Dashboard):** Customer service support. Resolves complaints and manages user account lock status.
3. **Station Owner (Web Dashboard):** Manages pricing, chargers, active connectors, bookings, and tracks revenue reports for their owned stations.
4. **User (Mobile App):** Finds charging stations using GPS, reserves charging slots, scans QR codes to start charging, monitors progress in real-time, and pays via MoMo.
5. **Guest (Mobile App):** Searches, filters, and views stations on the map, and registers for a new account.

---

## 🏗 Directory Structure

```text
lvtn-251-evgo/
├── evgo/                   # Backend (Spring Boot & Spring Modulith)
├── website/                # Web Dashboard (Next.js for Admin, Staff, & Owners)
├── mobile-app/             # Mobile App (React Native/Expo for Users & Guests)
└── report/                 # Graduation project reports and design diagrams
```

---

## 🛠 Technology Stack

| Component            | Technology                                    |
| :------------------- | :-------------------------------------------- |
| **Backend**          | Java 21, Spring Boot 4.0, Spring Modulith 2.0 |
| **Database & Cache** | PostgreSQL, Redis                             |
| **Charger Protocol** | OCPP-1.6J (WebSocket-based communication)     |
| **Security & Auth**  | Spring Security + JWT, Google OAuth2          |
| **Payment API**      | MoMo One-Time Payment API (v3)                |
| **Web Dashboard**    | Next.js 15, TailwindCSS v4, TanStack Query    |
| **Mobile App**       | React Native (Expo ~54), Zustand              |

---

## 🚀 Quick Start Guide

### Prerequisites

- **Java JDK 21** & **Maven 3.9+**
- **Node.js** & **npm**
- **Docker** & **Docker Compose**

### Running the Project

#### 1. Database & Cache

Navigate to the `evgo` directory, create a `.env` file, edit its values as needed, and start PostgreSQL and Redis:

```bash
cd evgo
cp .env.example .env
# Open and edit .env to configure database credentials, JWT secrets, ports,...
docker compose up -d
```

#### 2. Backend (`evgo`)

Build and run the Spring Boot application:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`

#### 3. Web Dashboard (`website`)

Initialize and run the Next.js development server:

```bash
cd ../website
cp .env.example .env
# Open and edit .env to set backend API endpoints (e.g. NEXT_PUBLIC_API_URL)
npm install
npm run dev
```

- URL: `http://localhost:3000`

#### 4. Mobile App (`mobile-app`)

Initialize and run the Expo application:

```bash
cd ../mobile-app
cp .env.example .env
# Open and edit .env to set local network API endpoints (e.g. EXPO_PUBLIC_API_URL)
npm install
npx expo start
```

- Open using the **Expo Go** application on a physical device or simulator.

---

## 🧪 Architecture & Testing

Verify backend modularity boundaries and generate architecture diagrams:

```bash
cd evgo
# Run boundary verification tests
mvn test -Dtest=ModularityTests

# Generate PlantUML diagrams (located in target/spring-modulith-docs/)
mvn test -Dtest=DocumenterTests
```
