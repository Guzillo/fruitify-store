# Fruitify Store

Monorepo for the Fruitify Store application.

## Repository structure

| Directory | Description |
|---|---|
| [`backend/`](backend/) | Spring Boot REST API, PostgreSQL persistence, authentication, orders and Stripe payments |
| [`frontend/`](frontend/) | Frontend application workspace |

## Getting started

### Backend

```bash
cd backend
docker compose up -d db
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Before the first run, generate the RSA keys and configure the Stripe
environment variables as described in
[`backend/README.md`](backend/README.md).

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>

### Frontend

The frontend workspace is reserved for the client application. Its setup and
development commands will be documented in [`frontend/README.md`](frontend/README.md)
when the frontend stack is added.

## Development

Backend tests:

```bash
cd backend
./mvnw test
```

The backend README contains the detailed API overview, configuration
reference, database migration rules, test information and payment flow.
