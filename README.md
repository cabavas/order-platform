# order-platform

Event-driven order processing platform built with Spring Boot, designed as a
hands-on exercise in distributed systems patterns: event streaming, sagas,
idempotency, and observability.

## Overview

This project simulates a simplified e-commerce backend where orders flow
through multiple services asynchronously. Instead of a CRUD demo, the goal is
to practice the patterns that show up in real production systems:

- **Rich domain model** — business rules live in entities, not in services
- **Event-driven architecture** — services communicate through events
- **Saga pattern** — coordinated consistency without distributed transactions
- **Outbox pattern** — atomic persistence + event publication
- **Idempotency** — safe message reprocessing
- **Observability** — distributed tracing, metrics, structured logs

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0 |
| Persistence | Spring Data JPA + PostgreSQL 16 + Flyway |
| Messaging | Apache Kafka |
| Mapping | MapStruct |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI |
| Testing | JUnit 5, Testcontainers, AssertJ, Mockito |
| Build | Maven (multi-module) |
| Containerization | Docker Compose |

## Architecture

```
order-platform/
├── pom.xml                  # parent POM
├── docker-compose.yml       # infrastructure (PostgreSQL, Kafka)
├── README.md
├── order-service/           # order lifecycle and orchestration
├── payment-service/         # (planned) payment processing
├── inventory-service/       # (planned) stock reservation
├── notification-service/    # (planned) customer notifications
└── api-gateway/             # (planned) single entry point
```

### Package structure (per service)

```
com.github.cabavas.orderplatform.<service>/
├── <domain>/                # bounded context (e.g. "order")
│   ├── api/                 # controllers + DTOs
│   ├── domain/              # entities, value objects, domain exceptions
│   ├── mapper/              # DTO <-> entity mapping
│   ├── repository/          # data access interfaces
│   └── service/             # application services (orchestration)
└── shared/                  # cross-cutting concerns
```

## Current Status

### ✅ Implemented (order-service)

- Multi-module Maven setup with shared dependency management
- `Order` aggregate with validated state machine:
    - Transitions: `PENDING → PAYMENT_PROCESSING → PAID → CONFIRMED`
    - Cancellation allowed from `PENDING`, `PAYMENT_PROCESSING`, `PAID`, `CONFIRMED`
    - Failure state reachable from `PAYMENT_PROCESSING`
    - Automatic total calculation from items
- `OrderItem` with subtotal snapshot at creation time
- REST endpoints:
    - `POST /api/orders` — create order (returns `201`)
    - `GET /api/orders/{id}` — retrieve order (returns `200` or `404`)
    - `GET /api/orders?customerId={uuid}` — list orders by customer
- Bean Validation on request DTOs
- Global exception handler using Problem Details (RFC 7807)
- Flyway migration (`V1__create_orders_schema.sql`)
- Integration tests with Testcontainers (PostgreSQL real, not H2)
- Maven Failsafe for `*IT` separation from unit tests
- MapStruct-generated mappers (compile-time, no reflection)

### 🚧 In Progress

- Kafka integration for publishing `OrderCreated`

### 📋 Planned

**Phase 1 — Event-driven foundation**
- [ ] Add Kafka to `docker-compose.yml`
- [ ] Publish `OrderCreated` event when order is created
- [ ] Outbox pattern for atomic persistence + publication
- [ ] Idempotent event consumers
- [ ] Dead Letter Queue + retry with exponential backoff

**Phase 2 — Additional services**
- [ ] `payment-service` — consumes `OrderCreated`, publishes `PaymentApproved`/`PaymentFailed`
- [ ] `inventory-service` — consumes events, reserves stock
- [ ] `notification-service` — reacts to order lifecycle events
- [ ] Saga orchestration (or choreography) with compensating transactions

**Phase 3 — Platform hardening**
- [ ] `api-gateway` with Spring Cloud Gateway
- [ ] Rate limiting at the gateway
- [ ] Spring Security with JWT (Keycloak as identity provider)
- [ ] Resilience4j: circuit breaker, retry, bulkhead
- [ ] Distributed tracing (Micrometer Tracing + Zipkin/Tempo)
- [ ] Metrics (Prometheus) + dashboards (Grafana)
- [ ] Structured logging with correlation IDs

**Phase 4 — Deployment**
- [ ] Containerize each service (Dockerfile per module)
- [ ] Full `docker-compose.yml` orchestrating all services
- [ ] Kubernetes manifests (local kind/minikube)
- [ ] CI pipeline (GitHub Actions)

## Getting Started

### Prerequisites

- Java 21
- Docker + Docker Compose
- Maven (or use the bundled `mvnw` wrapper)

### Running the stack

Start infrastructure (PostgreSQL):

```bash
docker compose up -d
```

Run the application:

```bash
./order-service/mvnw -f order-service/pom.xml spring-boot:run
```

The service starts on port `8081`. Flyway applies migrations on startup.

### Running tests

```bash
./order-service/mvnw -f order-service/pom.xml clean verify
```

This runs:
- **Unit tests** (Surefire) — `*Test` / `*Tests`
- **Integration tests** (Failsafe) — `*IT`, using a real PostgreSQL via Testcontainers

Docker must be running.

### API Documentation

Swagger UI: `http://localhost:8081/swagger-ui.html`
OpenAPI spec: `http://localhost:8081/v3/api-docs`

### Health check

```bash
curl http://localhost:8081/actuator/health
```

## API Reference

### Create order

```http
POST /api/orders
Content-Type: application/json

{
  "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "items": [
    {
      "productId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "productName": "Mechanical Keyboard",
      "quantity": 1,
      "unitPrice": 450.00
    }
  ]
}
```

Response `201 Created`:

```json
{
  "id": "a1b2c3d4-...",
  "customerId": "3fa85f64-...",
  "status": "PENDING",
  "totalAmount": 450.00,
  "currency": "BRL",
  "createdAt": "2026-01-15T10:30:00Z",
  "items": [ ... ]
}
```

### Retrieve order

```http
GET /api/orders/{id}
```

Returns `200` with the order, or `404` with a Problem Details body.

### List orders by customer

```http
GET /api/orders?customerId={uuid}
```

Returns `200` with an array of orders, newest first.

### Error format

All errors follow RFC 7807 (Problem Details):

```json
{
  "type": "about:blank",
  "title": "Order not found",
  "status": 404,
  "detail": "Order not found: a1b2c3d4-...",
  "instance": "/api/orders/a1b2c3d4-..."
}
```

Validation errors include an extra `errors` map with field-level messages.

## Design Decisions

### Why UUID primary keys?

Client-generatable identifiers. Essential for the event-driven phase — the
service can assign an ID and publish an event before committing, without
round-tripping to the database for a sequence.

### Why `TIMESTAMPTZ` and `Instant`?

Store instants with timezone awareness. `TIMESTAMP` without timezone causes
subtle bugs when servers span zones or DST changes.

### Why no public setters on entities?

State changes must go through intention-revealing methods
(`markAsPaid`, `cancel`, `addItem`). This centralizes validation and prevents
invalid states from being constructed externally.

### Why `@Version` for optimistic locking?

Concurrent writes to the same order (payment + cancellation, for example) must
not silently overwrite each other. Optimistic locking surfaces conflicts at
commit time instead of losing data.

### Why Testcontainers instead of H2?

H2 doesn't support PostgreSQL-specific features (native UUID, `TIMESTAMPTZ`,
`ON DELETE CASCADE` semantics, JSON operators). Tests that pass on H2 can fail
on production PostgreSQL. Testcontainers runs a real PostgreSQL — slower to
start, but reliable.

### Why the `Outbox` pattern (planned)?

The database and the message broker cannot participate in a shared
transaction. Naive publication (save → publish) risks losing events if the
broker is unavailable after the commit. The Outbox pattern persists events in
the same transaction as the aggregate change, and a separate publisher
delivers them with at-least-once semantics.

## License

Personal study project — no license.