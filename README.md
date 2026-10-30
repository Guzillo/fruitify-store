# 🍏 Fruitify Store

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Stripe](https://img.shields.io/badge/Stripe-Checkout_%26_Webhooks-635BFF?logo=stripe&logoColor=white)](https://stripe.com/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-PostgreSQL_2.0-2C3E50?logo=testcontainers&logoColor=white)](https://testcontainers.com/)
[![Vitest](https://img.shields.io/badge/Vitest-5-6E9F18?logo=vitest&logoColor=white)](https://vitest.dev/)

A modern, production-grade full-stack e-commerce grocery platform engineered with **Java 25**, **Spring Boot 4.1**, and **Angular 22**.

Unlike generic CRUD portfolio apps, Fruitify Store addresses production-level distributed challenges: **deadlock-free inventory concurrency**, **asymmetric RSA-2048 signed JWT authentication (RS256)**, **Stripe webhook idempotency**, and a **three-tier test pyramid** with Testcontainers and Vitest.

---

## ⚡ Architectural & Engineering Highlights

* **Deadlock-Free Stock Reservation**: Cart checkout acquires pessimistic write locks (`SELECT ... FOR UPDATE`) with deterministic ID sorting to eliminate database deadlocks during high-concurrency races. Verified with multi-threaded integration stress tests (`OrderConcurrencyTest`).
* **Stateless Asymmetric JWT Authentication**: Built as an **OAuth2 Resource Server** issuing and validating **RS256 (RSA-2048)** signed JWTs, featuring role hierarchy (`ROLE_ADMIN` $\implies$ `ROLE_USER`) and database-backed refresh token rotation.
* **Financial Precision & Idempotency**: Stripe hosted checkout sessions with HMAC-SHA256 signature verification on webhooks. Currency calculations are handled exclusively with `BigDecimal` down to minor units (grosze) with zero float drift.
* **Modern Reactive UI**: Angular 22 standalone architecture powered by **Signals** for fine-grained reactivity, functional HTTP interceptors (automatic token renewal on `401`), and strict TypeScript models mirroring backend DTOs.
* **Database Evolution & Anti-Drift**: 19 Flyway SQL schema migrations validated against Hibernate (`ddl-auto=validate`). Zero auto-generation surprises.

---

## 🏗️ System Architecture

```mermaid
flowchart LR
    Client["Angular 22 Client<br/>(Signals + Interceptors)"]
    API["Spring Boot 4.1 API<br/>(OAuth2 Resource Server)"]
    DB[("PostgreSQL 18<br/>(Pessimistic Locks)")]
    Stripe["Stripe Gateway<br/>(Hosted Checkout)"]

    Client -->|"Bearer JWT / Refresh"| API
    API -->|"Flyway Migrations / JPA"| DB
    Client -.->|"Redirects"| Stripe
    Stripe -->|"HMAC-SHA256 Webhook"| API
```

---

## 🛠️ Tech Stack Overview

| Layer | Technologies | Rationale / Key Design |
|---|---|---|
| **Backend** | Java 25, Spring Boot 4.1.1, Spring Security 6 | Records, pattern matching, OAuth2 Resource Server, Nimbus RSA-2048 JWT |
| **Frontend** | Angular 22, TypeScript 6, RxJS 7.8, Vite/Vitest | Standalone components, Signals reactivity, functional guards |
| **Persistence** | PostgreSQL 18, Flyway, Spring Data JPA | Transactional row-level locks, proxy-safe JPA identity |
| **Payments** | Stripe Java SDK 34 | Hosted Checkout Sessions, cryptographic webhook handling |
| **Testing** | Testcontainers 2.0.5, JUnit 5, Vitest, Mockito | Real PostgreSQL container integration, concurrency stress tests |

---

## 🚀 Quick Start

### Prerequisites
* **JDK 25**, **Node.js** (LTS) & **npm**
* **Docker** & Docker Compose
* **OpenSSL** (for RSA key generation)

### 1. Start Database & Backend
```bash
cd backend
docker compose up -d db

# Generate local RSA keys (first run only)
mkdir -p src/main/resources/certs
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/certs/private.pem
openssl rsa -pubout -in src/main/resources/certs/private.pem -out src/main/resources/certs/public.pem

# Run Spring Boot (seeds demo products & accounts)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
* **API**: `http://localhost:8080`
* **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 2. Start Frontend
```bash
cd frontend
npm install
npm start
```
* **Web App**: `http://localhost:4200`

### Demo Credentials (`dev` profile)
| Role | Email | Password | Permissions |
|---|---|---|---|
| **User** | `user@email.com` | `pass1` | Browse products, manage cart, checkout, view own orders |
| **Admin** | `admin@email.com` | `pass2` | Full access + Product/Category CRUD, global orders management |

---

## 🧪 Testing

```bash
# Backend unit, integration & multi-threaded concurrency tests
cd backend && ./mvnw test

# Frontend component & HTTP testing suite (Vitest)
cd frontend && npm test
```
