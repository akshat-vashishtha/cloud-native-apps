# Project Idea & Problem Space

## Problem
Building a modern, highly scalable cloud-native microservice architecture on Azure Kubernetes Service (AKS) to master enterprise backend engineering patterns: Clean Architecture (Hexagonal), Domain-Driven Design (DDD), 15-Factor microservice rules, polyglot persistence, hybrid synchronous/asynchronous communication, and AI agentic flows.

## Users
- **End-users**: Real-time interactive chat over WebSockets/STOMP, executing business operations, and consuming AI conversational intelligence and analytics.
- **Administrators & Operators**: Managing system access, RBAC permissions, audit trails, and monitoring real-time KPI metrics.

## Goals
- **Clean Architecture & Inward Dependency**: Implement strict concentric layer boundaries with zero framework imports in domain models and zero entity leaks.
- **Dual-Zone Security**:
  - Public Edge: `Gateway-Service` terminating SSL and validating Session/Cookie/JWT authentication.
  - Private Internal Mesh: Zero-trust microservice communication with internal HMAC signature verification and claims propagation.
- **Hybrid Communication Protocols**:
  - **Public Ingress**: REST API & STOMP WebSockets.
  - **Internal Sync**: gRPC for high-throughput, low-latency inter-service queries and commands.
  - **Internal Async**: Apache Kafka for distributed event streaming, choreographies, and decoupling.
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
   - `Gateway-Service` (REST API & STOMP routing, Authentication: Session & Cookie / JWT).
3. **Private Internal Mesh** (Secured via Internal HMAC / Claims):
   - `User-Rbac-Service`: Identity, roles, permissions, token verification.
   - `Chat-Communication-Service`: Real-time bidirectional STOMP/WebSocket chat.
   - `Business-Operation-Service`: Core transactional business workflows.
   - `KPI-Analytics-Service`: Real-time KPI aggregation and stream analytics.
   - `Intelligence-Ai-Service`: Spring AI 2.0 + GPT-4, Tool Calling, Vector Retrieval.
4. **Communication & Data Infrastructure**:
   - **Kafka**: Asynchronous event streams.
   - **gRPC**: Synchronous low-latency internal RPC.
   - **Redis**: Caching, pub/sub, and semantic search cache.
   - **Qdrant**: High-performance vector database.
   - **MongoDB**: Primary document persistence.
