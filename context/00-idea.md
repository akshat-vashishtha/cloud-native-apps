# Project Idea & Problem Space

## Problem
Building a modern, highly scalable cloud-native microservice architecture on Azure Kubernetes Service (AKS) to master enterprise backend engineering patterns: Clean Architecture (Hexagonal), Domain-Driven Design (DDD), 15-Factor microservice rules, polyglot persistence, hybrid synchronous/asynchronous communication, and AI agentic flows.

## Users
- **End-users**: Real-time interactive chat over WebSockets/STOMP, executing business operations, and consuming AI conversational intelligence and analytics.
- **Administrators & Operators**: Managing system access, RBAC permissions, audit trails, and monitoring real-time KPI metrics.

## Goals
- **Clean Architecture & Inward Dependency**: Implement strict concentric layer boundaries with zero framework imports in domain models and zero entity leaks.
- **OAuth2 Dual-Zone Security**:
  - Public Edge: `Gateway-Service` BFF — OAuth2 client, opaque `APP_SESSION` (Redis), TokenRelay of access tokens.
  - Authorization Server: separate `oauth2-authorization-server` issues JWTs (JWKS); hosts login form.
  - Private Mesh: downstream services are OAuth2 JWT resource servers (verify via AS JWKS / `kid`). HMAC is retired for identity.
  - Browser SPA (`web/`): cookie-only; never stores access/refresh tokens.
- **Hybrid Communication Protocols** (targets in `context/platform-best-practices.md`):
  - **Public Ingress**: REST API & **bidirectional** STOMP (client invoke + server push by `userId`).
  - **Internal Sync**: gRPC **servers + clients** (deadlines, interceptors) for AI tools and mesh RPC.
  - **Internal Async**: Kafka with outbox, idempotent consumers, retry/DLT, trace headers.
  - **Data access**: MongoDB Criteria/`MongoTemplate` for complex/atomic queries; Redis namespaced TTLs.
  - **Agentic AI**: Spring AI `ChatClient` routing + tools over gRPC + HITL + STOMP token stream.
- **Polyglot Persistence & Advanced AI Data Tier**:
  - **MongoDB**: Primary document persistence (Database-per-service isolation).
  - **Redis**: Multi-purpose cache (session management, distributed locks, low-latency read caching, and semantic response caching).
  - **Qdrant Vector Store**: Dedicated vector database for embeddings, semantic similarity search, and RAG document retrieval.
- **AI-Native Capabilities**: `Intelligence-Ai-Service` powered by Spring AI 2.0 + GPT-4 integration, leveraging Qdrant for RAG and Redis for semantic caching.
- **Cloud-Native Deployment**: Production deployment on Azure Kubernetes Service (AKS) with automated CI/CD and telemetry.

## Constraints
- Hands-on technical mastery and architectural cleanliness are primary; keep services modular and decoupled.
- Strict database isolation per microservice.
- All configuration externalized via `@ConfigurationProperties` records and environment variables.

## Component Overview (Refined Architecture)
1. **Host**: Azure Cloud + Azure Kubernetes Service (AKS).
2. **Public Edge**:
   - `web/` SPA (Next.js) — Sign in redirect, register, cookie APIs.
   - `Gateway-Service` — BFF OAuth2 client, `APP_SESSION`, TokenRelay, REST/STOMP routing.
3. **Identity**:
   - `oauth2-authorization-server`: token issuance, JWKS, login form.
   - `User-Rbac-Service`: users, passwords, RBAC only (no token minting); internal claims for AS.
4. **Private Internal Mesh** (JWT resource servers):
   - `Chat-Communication-Service`: Real-time bidirectional STOMP/WebSocket chat + outbox + Redis presence (**implemented**).
   - `Business-Operation-Service`: Core transactional business workflows + atomic Criteria outbox (**implemented**).
   - `KPI-Analytics-Service`: Txn velocity (Lua) + daily metrics + chat engagement consumer + DLT (**implemented**).
   - `Intelligence-Ai-Service`: Spring AI Agentic Concierge, `@Tool` over gRPC clients, HITL confirmation, STOMP token stream, Redis semantic cache (**implemented**).
5. **Communication & Data Infrastructure**:
   - **Kafka**: Asynchronous event streams.
   - **gRPC**: Synchronous low-latency internal RPC.
   - **Redis**: Caching, pub/sub, and semantic search cache.
   - **Qdrant**: High-performance vector database.
   - **MongoDB**: Primary document persistence.
