# Production-Grade Microservices Patterns with Spring Boot 4

Event-driven, production-focused microservices architecture using Java 25 and Spring Boot 4. Focus areas: failure handling, idempotency, retry, dead-letter topics (DLT) and safe recovery. This repository validates proven distributed-system patterns on the latest JVM and Spring ecosystem.

## Table of Contents
- [Key Architecture Principles](#key-architecture-principles)
- [Services Overview](#services-overview)
- [Event-Driven Saga Choreography](#event-driven-saga-choreography)
- [Kafka, Retry & DLT Strategy](#kafka-retry--dlt-strategy)
- [Security Model](#security-model)
- [Idempotency Strategy](#idempotency-strategy)
- [Technology Stack](#technology-stack)
- [Important Note on Java 25 & Spring Boot 4](#important-note-on-java-25--spring-boot-4)
- [What This Project Proves](#what-this-project-proves)
- [How to Run (High Level)](#how-to-run-high-level)

---

## Key Architecture Principles
- Gateway-first design
- Event-driven communication (Kafka)
- Saga choreography (no orchestration)
- No synchronous service-to-service calls
- Idempotency at every layer
- Kafka-native retry & DLT
- Manual, audited DLT replay
- Recovery-first mindset

---

## Services Overview

### 1. API Gateway (Reactive)
**Technology**
- Spring Cloud Gateway
- Spring WebFlux
- Spring Security

**Responsibilities**
- Single entry point
- JWT validation via JWKS
- Rate limiting, role-based routing, request logging

**Notes**
- Reactive by design (high concurrency)
- No DB access, no Kafka interaction

### 2. Auth Service (JWT RS256 + JWKS)
**Responsibilities**
- Issue JWT tokens
- Maintain private signing keys
- Expose public keys via JWKS

**Why RS256**
- No shared secrets, independent token validation, safe key rotation

### 3. Order Service (Saga Initiator)
**Responsibilities**
- Order lifecycle ownership
- HTTP idempotency (Idempotency-Key)
- Outbox pattern, saga timeout compensation

**Patterns**
- Outbox (DB + event atomicity), Kafka publishing, typed domain events

### 4. Payment Service (Critical Path)
**Responsibilities**
- Process payments, maintain immutable ledger
- Emit payment events, handle retries and DLT

**Key Guarantees**
- Append-only ledger, multi-layer idempotency, separation of business vs technical failures

### 5. Notification Service (Side-Effect Isolation)
**Responsibilities**
- Consume payment events, send notifications, track delivery attempts

**Design Goal**
- Failures here must never block business flow

### 6. DLT Replay Admin Service
**Responsibilities**
- ADMIN-only access
- Replay by topic/partition/offset
- Re-publish to original topic without payload mutation
- Full audit trail

**Important**
- Replay is a human decision, not automatic

---

## Event-Driven Saga Choreography
- Choreography (no central coordinator)
- Services react to events and own decisions
- Failures are isolated and handled locally

---

## Kafka, Retry & DLT Strategy
**Kafka**
- Single integration backbone, at-least-once delivery, replayable history

**Retry**
- Spring Kafka DefaultErrorHandler
- Offset-aware, non-blocking, backoff-based

**Dead Letter Topics (DLT)**
- Used only for technical failures
- Payload preserved, failure metadata in headers
- Safe for audited replay

---

## Security Model
- JWT RS256 everywhere, JWKS validation in all services
- Role-based access control
- ADMIN-only operational endpoints
- Each service acts as a resource server
- No shared secrets

---

## Idempotency Strategy
Idempotency enforced at multiple layers:
- HTTP: Idempotency-Key
- Kafka: consumer-side deduplication logic
- Database: unique constraints
- Ledger: immutable writes

Prevents duplicate orders, double payments, corrupted saga state.

---

## Technology Stack
Layer | Technology
--- | ---
Language | Java 25
Framework | Spring Boot 4
Gateway | Spring Cloud Gateway (WebFlux)
Messaging | Apache Kafka
Security | Spring Security + JWT
Persistence | MySQL
Cache | Redis
Retry | Spring Kafka
Observability | Micrometer
Serialization | JSON (Jackson)

---

## Important Note on Java 25 & Spring Boot 4
This project targets Java 25 and Spring Boot 4 to validate architectural correctness and failure-handling semantics (Kafka, DLT, retry, saga) without coupling to experimental APIs. The goal is correctness and reliability over showcasing bleeding-edge framework features.

---

## What This Project Proves
- Framework upgrades do not require architecture rewrites
- Reliability comes from design, not annotations
- DLT and replay tooling are first-class citizens
- Observability beats ad-hoc logging
- Failure handling must be explicit and testable

---

## How to Run (High Level)
1. Start infrastructure: Kafka, MySQL, Redis  
2. Run services in order:
    1. Auth Service  
    2. API Gateway  
    3. Order Service  
    4. Payment Service  
    5. Notification Service  
    6. DLT Replay Admin  
3. Each service is independently deployable.

--- 

For operational details (configuration, schemas, deployment, DLT replay procedures), consult each service's subdirectory README and the operational admin docs in this repository.
