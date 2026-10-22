# Microservices Interview Questions (10-Year Java Lead, Spring Boot)

## Table of Contents

- [1. Design and Decomposition](#1-design-and-decomposition)
- [2. Communication](#2-communication)
- [3. Data Management](#3-data-management)
- [4. Resilience and Fallback](#4-resilience-and-fallback)
- [5. Configuration and Deployment](#5-configuration-and-deployment)
- [6. Security](#6-security)
- [7. Performance and Scalability](#7-performance-and-scalability)
- [8. Observability and Operations](#8-observability-and-operations)
- [9. Testing](#9-testing)
- [10. Architectural Scenarios](#10-architectural-scenarios)
- [11. System Design Scenarios](#11-system-design-scenarios)

---

## 1. Design and Decomposition

1. How do you decide service boundaries? How do you apply DDD concepts such as bounded contexts and aggregates?
2. How do you recognise a distributed monolith, and how do you fix one?
3. Monolith vs modular monolith vs microservices: when is each the right choice?
4. How do you size a microservice? What are the risks of too-fine-grained services?
5. What is the strangler fig pattern? How would you use it to migrate a legacy system?
6. How do you handle shared code and libraries across services without coupling them?
7. How do you handle API versioning and backward compatibility between teams?
8. How do you organise teams and ownership (Conway's law, you build it you run it)?
9. What is the role of an API Gateway vs a BFF (Backend for Frontend)? When do you need both?
10. Which design patterns do you actually use in microservices (Saga, Outbox, CQRS, Sidecar, Anti-Corruption Layer), and why?

## 2. Communication

1. Synchronous (REST/gRPC) vs asynchronous (messaging) communication: how do you choose?
2. REST vs gRPC vs GraphQL for service-to-service calls: trade-offs?
3. How do you avoid long synchronous call chains and cascading latency?
4. How do you implement service discovery in Spring Boot on Kubernetes (client-side vs server-side, Spring Cloud vs native K8s)?
5. Feign vs RestClient vs WebClient: when do you use each?
6. How do you handle timeouts, retries, and idempotency on inter-service calls?
7. How do you design idempotent APIs (idempotency keys, PUT vs POST)?
8. How do you document and govern APIs (OpenAPI, contract-first, breaking-change checks)?
9. How do you handle backward-compatible changes in gRPC/Protobuf contracts?
10. How do you propagate context (correlation ID, user identity, tenant) across sync and async calls?

## 3. Data Management

1. Why does each microservice own its data? What are the problems with a shared database?
2. How do you maintain consistency without distributed transactions (Saga, eventual consistency)?
3. Choreography vs orchestration sagas: how do you decide, and how do you handle compensation?
4. How do you handle queries that need data from multiple services (API composition, CQRS, materialised views)?
5. Polyglot persistence: how do you choose between MongoDB, a relational DB, and Redis per service?
6. How do you handle schema migrations (Flyway/Liquibase) with zero-downtime deployments?
7. What is the transactional outbox, and why is it needed?
8. How do you manage reference or master data shared across services?
9. How do you design caching with Redis (cache-aside, TTL, invalidation, stampede protection)?
10. How do you handle data deletion and GDPR across multiple services?

## 4. Resilience and Fallback

1. What are circuit breaker, retry, bulkhead, rate limiter, and time limiter? How do you combine them in Resilience4j and in what order?
2. How do you decide the fallback behaviour for a failing dependency (cached data, default value, degraded feature, queue for later)?
3. How do you prevent retry storms and cascading failures?
4. How do you design for graceful degradation when a non-critical service is down?
5. What is backpressure, and how do you apply it in a Spring Boot service?
6. How do you implement health checks, readiness probes, and liveness probes correctly? What breaks when a readiness probe depends on downstream services?
7. How do you handle partial failures in a distributed workflow?
8. How do you design idempotency so retries are safe?
9. How do you test resilience (chaos engineering, fault injection)?
10. How do you handle a poison request or malicious spike on one endpoint without hurting other tenants?

## 5. Configuration and Deployment

1. How do you manage configuration across environments (Spring Cloud Config, K8s ConfigMaps/Secrets, Azure App Configuration, Key Vault)?
2. How do you refresh configuration at runtime, and what are the risks?
3. How do you manage secrets securely in AKS (Key Vault CSI driver, Workload Identity)?
4. Explain Spring Boot profiles and externalised configuration precedence.
5. Which deployment strategies do you use (rolling, blue-green, canary), and how do you roll back safely?
6. How do you handle backward-compatible DB and API changes during rolling deployments?
7. What goes into a good Dockerfile for a Spring Boot service (layered JARs, JRE image, non-root user, memory settings)?
8. How do you tune JVM memory and CPU limits in containers (heap vs container memory, OOMKilled)?
9. What are Helm and Kustomize used for, and how do you structure manifests across many services?
10. How do you design CI/CD pipelines for dozens of services (monorepo vs polyrepo, pipeline templates, quality gates)?
11. How do you do feature toggles, and what is the cost of long-lived toggles?

## 6. Security

1. How do you secure service-to-service and client-to-service communication (OAuth2, JWT, mTLS)?
2. Where do you validate tokens: at the gateway, in each service, or both?
3. How do you propagate user identity and roles between services?
4. How do you design authorization (RBAC, ABAC, scopes) across microservices?
5. How do you handle secrets rotation and certificate renewal?
6. What are the main API security risks (OWASP API Top 10), and how do you mitigate them?
7. How do you secure the network inside a cluster (network policies, zero trust)?
8. How do you handle audit logging and PII in logs?

## 7. Performance and Scalability

1. How do you find the bottleneck in a slow request that crosses five services?
2. How do you scale stateless vs stateful services (HPA, KEDA, partitioning)?
3. Which metrics should drive autoscaling besides CPU?
4. How do you design for high read traffic (caching layers, read replicas, CDN)?
5. How do you manage connection pools (HTTP, DB, Redis) across many instances so you do not exhaust downstream resources?
6. What is the N+1 call problem between services, and how do you avoid it?
7. How do you choose between blocking (MVC) and reactive (WebFlux), and where do virtual threads fit?
8. How do you reduce JVM startup time and memory footprint (CDS, GraalVM native image, lazy init)?
9. How do you load test microservices, and what do you measure (p95/p99, saturation, error rate)?
10. How do you apply rate limiting and quotas at the gateway and service level?

## 8. Observability and Operations

1. Explain the three pillars of observability (logs, metrics, traces). What tools do you use?
2. How do you implement distributed tracing in Spring Boot (Micrometer Tracing, OpenTelemetry)?
3. How do you design structured logging with correlation IDs?
4. Which SLIs, SLOs, and alerts do you define for a service? How do you avoid alert fatigue?
5. How do you debug a production issue that only reproduces across multiple services?
6. How do you do on-call and incident management (runbooks, postmortems)?
7. How do you monitor dependencies and detect a silent failure (a service is up but returning wrong data)?
8. How do you track cost and resource usage across services?

## 9. Testing

1. What is your testing strategy for microservices (unit, integration, contract, end-to-end)?
2. What is consumer-driven contract testing (Pact, Spring Cloud Contract), and why is it better than end-to-end tests?
3. How do you use Testcontainers for MongoDB, Redis, and Kafka in integration tests?
4. How do you test in production safely (canary, shadow traffic, synthetic checks)?
5. How do you handle test data and environments for 30+ services?
6. How do you test sagas and failure paths?

## 10. Architectural Scenarios

1. Your team inherits a 10-year-old monolith. Leadership wants microservices in 12 months. How do you decide what to extract first, and how do you manage risk?
2. A single user request fans out to eight services and p99 latency is 6 seconds. How do you analyse and reduce it?
3. A downstream service goes down and takes your whole platform with it. What design changes would have prevented this?
4. Two teams both claim ownership of the "Customer" entity and each keeps their own copy. How do you resolve this?
5. A critical bug fix requires coordinated changes in four services. How do you release it safely?
6. Your services keep calling each other in circles (A to B to C to A). How do you detect and fix it?
7. A new regulation requires all PII to stay within one region. How does it change your architecture?
8. The business wants to add a feature that needs data from five services in a single screen. How do you design it?
9. You discover two services are always deployed together. What does it tell you, and what do you do?
10. Shared libraries have become a bottleneck: each change forces every team to upgrade. How do you break out of that?
11. A third-party API you depend on has frequent outages and tight rate limits. How do you design around it?
12. Costs on AKS have doubled in six months. How do you investigate and reduce them without hurting reliability?

## 11. System Design Scenarios

1. Design an e-commerce platform (catalog, cart, order, payment, inventory, notification) as microservices. Cover boundaries, data ownership, consistency, and failure handling.
2. Design a ride-booking or food-delivery backend with real-time location updates and matching.
3. Design a multi-tenant SaaS platform with tenant isolation, per-tenant rate limits, and noisy-neighbour protection.
4. Design a payment gateway integration that is safe against duplicate charges, timeouts, and partial failures.
5. Design a chat and AI-assistant platform (gateway, auth, chat, AI orchestration, notification) on AKS with Kafka, Redis, and MongoDB.
6. Design an API gateway layer: authentication, routing, rate limiting, aggregation, and versioning for 50+ services.
7. Design a centralised authentication and authorisation platform for dozens of services and multiple client types.
8. Design a booking system (tickets, hotels, seats) that avoids double-booking under heavy concurrency.
9. Design a file upload and processing pipeline (virus scan, transcoding, metadata extraction) with retries and status tracking.
10. Design a feature-flag and configuration platform that lets teams change behaviour without redeploying.
11. Design a zero-downtime migration of a core service's database from relational to MongoDB while traffic is live.
12. Design a multi-region active-active deployment of an order service, including data replication and conflict handling.
