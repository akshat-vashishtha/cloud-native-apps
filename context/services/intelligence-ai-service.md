# Service: Intelligence-Ai-Service
Status: implemented   Owner: @Sophia, @Elena, @Leo (Reviewer: @Alex)   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md, context/platform-best-practices.md

## Summary
Multi-Agent Financial Concierge built on **Spring AI 2.0.1** following `.agents/skills/spring-agentic-ai-expert/SKILL.md`. Implements a full **Advisor Pipeline** (`SafeGuardAdvisor` $\rightarrow$ `MessageChatMemoryAdvisor` (`MessageWindowChatMemory` backed by `MongoChatMemoryRepository`) $\rightarrow$ `SimpleLoggerAdvisor` $\rightarrow$ `AiAuditAdvisor`), externalized `.st` Prompt Templates (`classpath:prompts/*.st`), **Intent Routing + Strategy Pattern** (`IntentRouterAgent` $\rightarrow$ `FinancialConciergeAgent`, `TransactionSpecialistAgent`, `CardSecuritySpecialistAgent`), **Evaluator-Optimizer Guardrail** (`RiskEvaluatorAgent`), **Least-Privilege `@Tool` Segregation** over gRPC clients (`ConciergeKnowledgeTools`, `TransactionOperationTools`, `CardSecurityTools`), **2-Step Human-in-the-Loop (HITL)** confirmation tokens in Redis, and **Redis Semantic Cache** + **MongoDB Criteria** working memory.

## Decisions

- **SVC-AI-001**: **Dedicated `@Bean` `ChatClient` Per Agent + Advisor Stack** (`SpringAiConfig.java`) — Configures `routerChatClient`, `conciergeChatClient`, `transactionChatClient`, `securityChatClient`, and `evaluatorChatClient` with `SafeGuardAdvisor` (OWASP LLM01 prompt injection protection), `MessageChatMemoryAdvisor` (`MessageWindowChatMemory`), and `AiAuditAdvisor` (`CallAdvisor` recording Micrometer `ai.prompt.latency` & `ai.tokens.used`).
- **SVC-AI-002**: **Multi-Agent Strategy Pattern (`SpecialistAiAgent`)** — Each specialist agent (`FinancialConciergeAgent`, `TransactionSpecialistAgent`, `CardSecuritySpecialistAgent`) is an independent Spring `@Component` auto-registered in `AiConciergeServiceImpl` via `EnumMap<AgentCategory, SpecialistAiAgent>` (Open/Closed Principle).
- **SVC-AI-003**: **Least-Privilege `@Tool` Segregation (OWASP LLM06)** — `ConciergeKnowledgeTools` only exposes read-only profile & RAG search; `TransactionOperationTools` exposes balance check & HITL transfer staging; `CardSecurityTools` only exposes HITL card lock/unlock staging.
- **SVC-AI-004**: **Externalized `.st` Prompt Templates** — All system prompts live in `src/main/resources/prompts/*.st` (`router-system.st`, `concierge-agent-system.st`, `transaction-agent-system.st`, `security-agent-system.st`, `evaluator-agent-system.st`).
- **SVC-AI-005**: **Evaluator-Optimizer Compliance Loop (`RiskEvaluatorAgent`)** — Verifies candidate responses against OWASP LLM05 / policy rules using structured `.entity(PolicyEvaluation.class)` and refines non-compliant outputs before returning.
- **SVC-AI-006**: **HITL 2-Step Confirmation + STOMP Streaming** — Mutations stage `PendingConfirmation` in `RedisConfirmationTokenService` (`ai.confirmations.v1` $\rightarrow$ `/user/{userId}/queue/confirmations`); token chunks stream via `ai.stream.v1` $\rightarrow$ `/user/{userId}/queue/ai-stream`.
- **SVC-AI-007**: **Composite Redis HITL Token Key (`ai:confirmation:{userId}:{token}`)** — Isolates token storage to the user namespace in Redis, preventing unauthorized token deletion via `getAndDelete`.
- **SVC-AI-008**: **Zero Entity Leaks via `ChatTurnResponseDto`** — Maps domain `ChatTurn` to application DTO on `/api/v1/ai/history/{sessionId}`.
- **SVC-AI-009**: **Bounded Evaluator-Optimizer Loop (`maxIterations = 2`)** — Iterative loop in `RiskEvaluatorAgent` that re-evaluates refined responses until acceptable or max iterations reached.
- **SVC-AI-010**: **Pre-Stage RBAC Permission Verification (OWASP LLM06)** — Tools verify `transfer:execute` and `card:update_status` via `GrpcUserRbacClient.hasPermission` before creating staged HITL tokens.
- **SVC-AI-011**: **Qdrant VectorStore Startup Ingestion & Observation Masking** — Ingests seeded policy documents on startup (`@PostConstruct`) and configures `spring.ai.chat.observations.log-prompt: false` (OWASP LLM02).

## Plan
Approved: yes

- [x] Multi-agent Strategy architecture (`IntentRouterAgent`, 3 `SpecialistAiAgent` beans, `RiskEvaluatorAgent`)
- [x] Spring AI 1.0 GA Advisor pipeline (`SafeGuardAdvisor`, `MessageChatMemoryAdvisor`, `AiAuditAdvisor`)
- [x] Least-privilege `@Tool` segregation over gRPC (`:9093` banking, `:9091` user-rbac)
- [x] Externalized `.st` prompt templates & RAG policy grounding (`BankingKnowledgeRagService`)
- [x] HITL confirmation + Mongo Criteria memory + Redis semantic cache
- [x] Audit hardening: Composite Redis HITL keys, bounded evaluator loop, RBAC tool gates, vector seeding, DTO leak elimination, and clean exception naming
