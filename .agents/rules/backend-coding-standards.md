# Backend Engineering & Microservices Coding Standards

## 1. Clean 4-Layer Architecture for Microservices

### 1.1 Unidirectional Top-Down Layer Organization
All backend microservices must strictly adhere to a **4-Layer Clean Layered Architecture** (`presentation`, `application`, `domain`, `infrastructure`) with strict separation of concerns and zero entity leaks.

```
+-----------------------------------------------------------------------+
| 1. PRESENTATION / CONTROLLER LAYER (presentation/)                    |
|    - REST Controllers, gRPC Services, STOMP WebSocket Controllers     |
|    - Request / Response validation (@Valid), GlobalExceptionHandler   |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 2. APPLICATION / SERVICE LAYER (application/)                         |
|    - Service Interfaces (XService) & Implementations (XServiceImpl)   |
|    - Application DTOs (Request/Response records), Application Errors  |
|    - Orchestrates Domain models, Repositories, Caches, and Messaging  |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 3. DOMAIN LAYER (domain/)                                             |
|    - Pure Java Domain Entities, Value Objects (Records), Enums        |
|    - Domain Events & Domain Exceptions (Zero external framework leaks)|
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 4. PERSISTENCE & INFRASTRUCTURE LAYER (infrastructure/)               |
|    - MongoDB @Document Entities, Spring Data Repositories, Criteria   |
|    - Kafka Consumers/Publishers, Redis Cache/Session, gRPC Clients    |
|    - MapStruct Mappers, Security Filters, @ConfigurationProperties    |
+-----------------------------------------------------------------------+
```

### 1.2 Domain Layer (`domain/`)
- **Zero External Dependencies**: The domain layer must NEVER import framework-specific packages (e.g., no `org.springframework.*`, `jakarta.persistence.*`, `com.fasterxml.jackson.*`).
- **Entities**: Rich domain models with state and business behavior. Enforce invariants upon instantiation and state transitions.
- **Value Objects**: Immutable objects (Java `record`s) defined by their attributes rather than identity, self-validating via compact constructors.
- **Domain Enums & Exceptions**: Strongly typed status machines (`AccountStatus`, `CardStatus`, `UserStatus`) and domain invariant exceptions.

### 1.3 Application / Service Layer (`application/`)
- **Service Contract & Implementation**: Define a clean service interface (`XService`) and concrete `@Service` class (`XServiceImpl`) in `application/service/`.
- **Application DTOs**: Explicit Java `record`s in `application/dto/` for request and response payloads across API boundaries.
- **Application Exceptions**: Business workflow exceptions in `application/exception/`.

### 1.4 Presentation Layer (`presentation/`)
- **Inbound Controllers**: REST Controllers (`presentation/rest/`), gRPC server endpoints (`presentation/grpc/`), and STOMP WebSocket controllers (`presentation/websocket/`).
- **Anti-Corruption & Zero Entity Leaks**:
  - **NEVER** expose Domain Entities or MongoDB `@Document` classes directly in REST/gRPC responses.
  - **NEVER** annotate Domain Entities with `@Document` or persistence annotations.
  - Use dedicated MapStruct mappers or explicit mapping methods to translate between `API DTOs` $\leftrightarrow$ `Domain Models` $\leftrightarrow$ `Database @Document Entities`.

### 1.5 Persistence & Infrastructure Layer (`infrastructure/`)
- **`persistence/`**: MongoDB `@Document` classes, Spring Data repositories, and `MongoTemplate` + `Criteria` query repositories (`*CriteriaRepository` / `*CriteriaQueryAdapter`) for complex filters, atomic `findAndModify` outbox claims, and upserts.
- **`messaging/` & `listener/`**: Transactional outbox publishers, Kafka `@KafkaListener` consumers, and `DefaultErrorHandler` + DLT configurations.
- **`cache/`**: Redis services (semantic cache, velocity counters, idempotency locks, HITL confirmation tokens).
- **`grpc/`**: Outbound `@GrpcClient` client wrappers with deadlines.
- **`config/` & `security/`**: Pure `@ConfigurationProperties` records, OAuth2 Resource Server `SecurityConfig`, and JWT authority converters.

---

## 2. Package & Directory Structure Convention

```
com.learning.<service>/
├── presentation/                   # 1. Presentation / Inbound Layer
│   ├── rest/                       # REST Controllers & GlobalExceptionHandler
│   ├── grpc/                       # gRPC Server Implementations (if applicable)
│   └── websocket/                  # STOMP @MessageMapping Controllers (if applicable)
├── application/                    # 2. Application / Service Layer
│   ├── dto/                        # Request & Response Java Records
│   ├── exception/                  # Application Workflow Exceptions
│   └── service/                    # Service Interfaces (XService) & XServiceImpl
├── domain/                         # 3. Enterprise Domain Layer (Pure Java)
│   ├── model/                      # Domain Entities & Value Objects
│   ├── enums/                      # Domain Enumerations
│   ├── event/                      # Domain Event Records
│   └── exception/                  # Domain Invariant Exceptions
└── infrastructure/                 # 4. Persistence & Infrastructure Layer
    ├── persistence/
    │   ├── document/               # MongoDB @Document Entities
    │   ├── repository/             # Spring Data MongoDB Repositories
    │   └── criteria/               # MongoTemplate + Criteria Query Repositories
    ├── messaging/                  # Outbox Publishers & Kafka Producers/Consumers
    ├── cache/                      # Redis Cache / Lock / Token Services
    ├── grpc/                       # Outbound gRPC Clients (@GrpcClient)
    ├── mapper/                     # MapStruct Mappers (Domain <-> Document/DTO)
    ├── security/                   # OAuth2 Resource Server & JWT Converters
    └── config/                     # Spring @Configuration & @ConfigurationProperties
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
| **X. Dev/Prod Parity** | Keep development, staging, and production identical | Use Testcontainers for local testing against real backing services (MongoDB, Kafka, Redis). |
| **XI. Logs** | Treat logs as event streams | Output structured JSON logs directly to standard out (`stdout`); no local file appenders. |
| **XII. Admin Processes** | Run admin/management tasks as one-off processes | Seeders and index initializers executed safely on startup or as init-containers. |
| **XIII. API First** | Contract-first design and specifications | OpenAPI 3.0 / Swagger or Protobuf specs defining contracts before implementation. |
| **XIV. Telemetry** | Built-in metrics, traces, and health checks | Spring Boot Actuator (`/actuator/health`, `/actuator/prometheus`) and OpenTelemetry tracing. |
| **XV. AuthN / AuthZ** | Zero-trust identity and security at boundaries | OAuth2 Authorization Server + BFF Gateway (`APP_SESSION` + `TokenRelay`) + downstream OAuth2 JWT Resource Servers. |

---

## 4. Pure `@ConfigurationProperties` Standards

- **Zero-Logic Data Holders**: Configuration properties classes must be immutable data holders with zero business logic.
- **Record or Immutable POJO**: Prefer Java `record`s with `@ConfigurationProperties`.
- **Validation at Startup**: Always annotate properties classes with `@Validated` and use JSR-380 validation annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Max`, `@Positive`, etc.).
- **Centralized YAML Defaults**:
  - All default configuration values and property keys must be explicitly documented and declared in `application.yml`.
  - Avoid scattered `@Value("${custom.property}")` annotations across domain, application, or infrastructure classes.

```java
@ConfigurationProperties(prefix = "app.payment-service")
@Validated
public record PaymentServiceProperties(
    @NotBlank String endpointUrl,
    @NotNull Duration timeout,
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
- Dynamic application state must use distributed stores (Redis).

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

---

## 6. Observability, Telemetry & Standardized Structured Logging

### 6.1 Structured JSON Logging
- All application logs must be emitted to `STDOUT` with contextual fields (`service`, `traceId`, `spanId`, `userId`, `status`).

### 6.2 Telemetry & Tracing
- **Trace Propagation**: Propagate trace headers across outbound HTTP, gRPC, and Kafka calls.
- **Health Probes**: Enable Kubernetes-compliant liveness and readiness probes under `/actuator/health`.

### 6.3 Logging Guidelines & Sensitive Data Protection
- **No Sensitive PII**: Never log passwords, API keys, bearer tokens, credit card numbers, or PII.
