# Backend Engineering & Microservices Coding Standards

## 1. Clean Architecture for Microservices

### 1.1 Concentric Layer Organization & Inward Dependency Rule
All backend microservices must strictly adhere to Uncle Bob's Clean Architecture and Hexagonal (Ports & Adapters) principles.
The dependency rule is absolute: **Source code dependencies must point only inward**, toward high-level policies and domain logic.

```
+-------------------------------------------------------------------------+
|  FRAMEWORKS & DRIVERS                                                   |
|  (Spring Boot, Web Server, DB Drivers, Message Brokers, Cloud SDKs)     |
|  +-------------------------------------------------------------------+  |
|  |  INTERFACE ADAPTERS                                               |  |
|  |  (REST Controllers, Kafka Listeners, JPA Repositories, Mappers)   |  |
|  |  +-------------------------------------------------------------+  |  |
|  |  |  APPLICATION BUSINESS RULES                                 |  |  |
|  |  |  (Use Cases / Interactors, Inbound & Outbound Ports, DTOs)   |  |  |
|  |  |  +-------------------------------------------------------+  |  |  |
|  |  |  |  ENTERPRISE BUSINESS RULES                           |  |  |  |
|  |  |  |  (Entities, Value Objects, Domain Enums & Exceptions) |  |  |  |
|  |  |  +-------------------------------------------------------+  |  |  |
|  |  +-------------------------------------------------------------+  |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```

### 1.2 Enterprise Business Rules (Domain Layer - Core)
- **Zero External Dependencies**: The domain layer must NEVER import framework-specific packages (e.g., no `org.springframework.*`, `jakarta.persistence.*`, `com.fasterxml.jackson.*`).
- **Entities**: Rich domain models with state and encapsulating business behavior. Enforce invariants upon instantiation and state transitions.
- **Value Objects**: Immutable objects (e.g., Java `record`s) defined by their attributes rather than an identity. Self-validating via compact constructors.
- **Domain Enums & Types**: Strongly typed domain concepts, status machines, and specific business identifiers (e.g., `OrderId`, `CustomerId`).
- **Domain Exceptions**: Specific unchecked business exceptions originating from domain invariant violations.

### 1.3 Application Business Rules (Application Layer)
- **Use Cases / Interactors**: Single Responsibility Principle (SRP) classes containing specific application workflows and transaction orchestrations (e.g., `CreateOrderUseCase`, `CancelOrderUseCase`).
- **Inbound Ports (Driving Ports)**: Java interfaces defining the contract for executing use cases (implemented by application services, called by controllers/consumers).
- **Outbound Ports (Driven Ports)**: Java interfaces defining requirements for external interactions (e.g., `OrderPersistencePort`, `PaymentGatewayPort`, `EventPublisherPort`).
- **Use Case DTOs / Commands / Queries**: Explicit data structures for request and response payloads across application boundaries.

### 1.4 Interface Adapters & Anti-Corruption Boundary (Zero Entity Leaks)
- **Inbound Adapters (Driving)**:
  - REST Controllers, GraphQL Resolvers, gRPC Services, and Event Listeners (Kafka/RabbitMQ/SQS).
  - Responsible for protocol negotiation, request validation (`@Valid`), and mapping web DTOs to Application Commands.
- **Outbound Adapters (Driven)**:
  - Persistence implementations (JPA, MongoDB, R2DBC), external REST/gRPC client adapters, and Message Producers.
  - Implement Outbound Port interfaces.
- **Anti-Corruption Layer & Zero Entity Leaks**:
  - **NEVER** expose Domain Entities directly to external clients via API endpoints.
  - **NEVER** map Domain Entities directly as JPA entities with database annotations (`@Entity`, `@Table`). Keep JPA entities (`OrderEntity`) separate from Domain entities (`Order`).
  - Use dedicated mapping layers (e.g., MapStruct or explicit mapper functions) to translate across layer boundaries:
    - `API Request DTO` $\rightarrow$ `Application Command` $\rightarrow$ `Domain Entity`
    - `Domain Entity` $\rightarrow$ `Database / JPA Entity` (and vice-versa)
    - `Domain Entity` $\rightarrow$ `Application Result` $\rightarrow$ `API Response DTO`

### 1.5 Frameworks & Drivers (Infrastructure Layer)
- Houses Spring Boot configurations, Security filters, persistence setup, third-party library integrations, and infrastructure plumbing.

---

## 2. Package & Directory Structure Convention

```
com.example.service/
├── domain/                         # Enterprise Business Rules (Pure Java)
│   ├── model/                      # Entities & Aggregate Roots
│   ├── valueobjects/               # Immutable Value Objects (Records)
│   ├── enums/                      # Domain Enumerations
│   └── exceptions/                 # Domain-specific Exceptions
├── application/                    # Application Business Rules
│   ├── port/
│   │   ├── in/                     # Inbound / Driving Ports & Commands
│   │   └── out/                    # Outbound / Driven Ports
│   └── service/                    # Use Case Interactors (Orchestration)
├── adapter/                        # Interface Adapters
│   ├── in/
│   │   ├── web/                    # REST Controllers, API DTOs, Web Mappers
│   │   └── event/                  # Event/Message Consumers
│   └── out/
│       ├── persistence/            # DB Adapters, JPA Entities, Spring Data Repos
│       ├── client/                 # External HTTP/gRPC Service Clients
│       └── messaging/              # Event/Message Publishers
└── infrastructure/                 # Frameworks & Configuration
    ├── config/                     # Spring Configuration & @ConfigurationProperties
    ├── security/                   # Security / Auth filters
    └── logging/                    # Logging filters, tracing setup
```

---

## 3. The 15-Factor Microservice Standards for Spring Boot

| Factor | Principle | Spring Boot Standard & Implementation |
| :--- | :--- | :--- |
| **I. Codebase** | One codebase tracked in revision control, many deploys | Single Git repository per deployable service with continuous integration pipelines. |
| **II. Dependencies** | Explicitly declare and isolate dependencies | Managed via Maven/Gradle BOMs (`spring-boot-dependencies`); no global system-level dependencies. |
| **III. Config** | Store configuration in the environment | Externalized configuration via `application.yml` and environment variables; zero hardcoded environments. |
| **IV. Backing Services** | Treat backing services as attached resources | Databases, caches (Redis), brokers (Kafka) accessed via connection URIs and credentials injected dynamically. |
| **V. Build, Release, Run** | Strictly separate build, release, and run stages | Immutable container images (OCI/Docker) built once, tagged, and deployed across environments without rebuilds. |
| **VI. Processes** | Execute app as one or more stateless processes | Services are 100% stateless; sessions/tokens stored externally (e.g., Redis, JWT). |
| **VII. Port Binding** | Export services via port binding | Self-contained embedded servers (Tomcat/Netty) listening on `server.port` (default: 8080). |
| **VIII. Concurrency** | Scale out via process model | Scale horizontally by increasing replicas; leverage Java 21+ Virtual Threads (`spring.threads.virtual.enabled=true`). |
| **IX. Disposability** | Fast startup and graceful shutdown | Fast startup with minimal blocking initialization; graceful shutdown configured (`server.shutdown=graceful`). |
| **X. Dev/Prod Parity** | Keep development, staging, and production identical | Use Testcontainers for local testing against real backing services (PostgreSQL, Kafka, Redis). |
| **XI. Logs** | Treat logs as event streams | Output structured JSON logs directly to standard out (`stdout`); no local file appenders. |
| **XII. Admin Processes** | Run admin/management tasks as one-off processes | Database migrations executed via Flyway/Liquibase on startup or as init-containers. |
| **XIII. API First** | Contract-first design and specifications | OpenAPI 3.0 / Swagger or AsyncAPI / Protobuf specs defining contracts before implementation. |
| **XIV. Telemetry** | Built-in metrics, traces, and health checks | Spring Boot Actuator (`/actuator/health`, `/actuator/prometheus`) and OpenTelemetry tracing. |
| **XV. AuthN / AuthZ** | Zero-trust identity and security at boundaries | Stateless token validation (OAuth2/JWT) with role-based access control (RBAC). |

---

## 4. Pure `@ConfigurationProperties` Standards

- **Zero-Logic Data Holders**: Configuration properties classes must be immutable data holders with zero business logic.
- **Record or Immutable POJO**: Prefer Java `record`s with `@ConfigurationProperties` or standard classes with `@ConstructorBinding`.
- **Validation at Startup**: Always annotate properties classes with `@Validated` and use JSR-380 validation annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Max`, `@Positive`, etc.).
- **Centralized YAML Defaults**:
  - All default configuration values and property keys must be explicitly documented and declared in `application.yml`.
  - Avoid scattered `@Value("${custom.property}")` annotations across domain, application, or adapter classes.

```java
@ConfigurationProperties(prefix = "app.payment-service")
@Validated
public record PaymentServiceProperties(
    @NotBlank String endpointUrl,
    @NotNull @DurationMin(millis = 100) Duration timeout,
    @Positive int maxRetryAttempts,
    @Valid SecurityProperties security
) {
    public record SecurityProperties(
        @NotBlank String apiKey,
        @NotBlank String secretKey
    ) {}
}
```

---

## 5. Stateless Processes, Concurrency & Graceful Disposability

### 5.1 Statelessness
- Never store user session data, cache locks, or state in static variables or local memory.
- In-memory caching (Caffeine) must be limited to read-through immutable reference data. Dynamic application state must use distributed stores (Redis).

### 5.2 Concurrency & Thread Safety
- **Virtual Threads**: For Spring Boot 3.2+ on Java 21+, enable virtual threads:
  ```yaml
  spring:
    threads:
      virtual:
        enabled: true
  ```
- **Thread Safety**: Avoid mutable shared state. All Spring `@Service`, `@Component`, and `@RestController` beans are singletons and must remain completely stateless and thread-safe.

### 5.3 Graceful Disposability & Lifecycle
- Configure graceful shutdown in `application.yml`:
  ```yaml
  server:
    shutdown: graceful
  spring:
    lifecycle:
      timeout-per-shutdown-phase: 30s
  ```
- Gracefully handle `SIGTERM`: Allow ongoing HTTP requests and in-flight Kafka batches to finish before tearing down database connection pools and container contexts.
- Cleanly release resources in `@PreDestroy` methods or via Spring `DisposableBean`.

---

## 6. Observability, Telemetry & Standardized Structured Logging

### 6.1 Structured JSON Logging
- All application logs must be emitted as single-line JSON objects to `STDOUT`.
- Standard mandatory fields in every log event:
  - `timestamp`: ISO-8601 UTC format
  - `level`: `DEBUG`, `INFO`, `WARN`, `ERROR`
  - `service`: Microservice name
  - `traceId` / `spanId`: Distributed tracing identifiers (W3C Trace Context / OpenTelemetry)
  - `thread`: Executing thread name
  - `logger`: Fully qualified class name
  - `message`: Contextual human-readable log message
  - `context`: Structured key-value arguments (e.g., `userId`, `orderId`, `status`)

### 6.2 Telemetry & Tracing
- **Trace Propagation**: Ensure trace headers (`traceparent`, `tracestate`, `b3`) are propagated across all outbound HTTP (via `RestClient` / `WebClient`) and Messaging (Kafka header) calls.
- **Metrics**: Expose Prometheus metrics under `/actuator/prometheus` including custom business meters (Timers, Counters, Gauges) registered via Micrometer `MeterRegistry`.
- **Health Probes**: Enable Kubernetes-compliant liveness and readiness probes:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health, info, prometheus, metrics
    endpoint:
      health:
        probes:
          enabled: true
        show-details: always
  ```

### 6.3 Logging Guidelines & Sensitive Data Protection
- **No Sensitive PII**: Never log passwords, API keys, bearer tokens, credit card numbers, or PII.
- **Log Levels Discipline**:
  - `ERROR`: Unhandled exceptions, broken contracts, downstream service outages requiring alerting.
  - `WARN`: Handled anomalies, transient retries, fallback activations, degraded performance.
  - `INFO`: Significant lifecycle events (application start/stop, completed business milestones).
  - `DEBUG`: Granular diagnostic information for troubleshooting in lower environments.
