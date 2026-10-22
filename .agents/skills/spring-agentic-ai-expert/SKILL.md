---
name: spring-agentic-ai-expert
description: >
  Master skill combining Spring AI 1.0 GA implementation patterns (ChatClient, Advisor pipelines,
  MessageWindowChatMemory, VectorStore RAG, @Tool & MCP servers, externalized .st PromptTemplates,
  Structured Output) with Agentic AI architectural best practices (Routing, Chain, Parallelization,
  Orchestrator-Workers, Evaluator-Optimizer), OWASP LLM Top 10 security guardrails (Prompt Injection,
  Excessive Agency & Human-in-the-Loop), and OpenTelemetry/Micrometer GenAI observability.
  Use whenever designing, reviewing, or implementing AI agents, LLM microservices, RAG pipelines,
  or Spring AI features.
---

# Spring Agentic AI & LLM Engineering Master Skill

Combines **Spring AI 1.0 GA Engineering** (`spring-ai`, `spring-ai-integration`, `spring-ai-mcp-server-patterns`) with **Industry Agentic AI, Security & Observability Best Practices** (`llm-security` OWASP Top 10 2025, `ai-observability`).

---

## 1. Spring AI 1.0 GA Core Rules (Zero Pre-GA Hallucinations)

### 1.1 Maven Coordinates (1.0 GA)
Always import `spring-ai-bom` and use the 1.0 GA `spring-ai-starter-*` coordinates:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>2.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
    <!-- Optional Vector Store (Qdrant / PGVector) or use SimpleVectorStore for local/dev -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-vector-store-qdrant</artifactId>
    </dependency>
</dependencies>
```

### 1.2 Dedicated `@Bean` Per Specialist `ChatClient`
Never dump all system prompts and all tools into a single generic `ChatClient` service method. Build dedicated, least-privilege `ChatClient` beans per specialist agent with a full **Advisor Pipeline**:

```java
@Configuration
public class AgenticAiConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        // 1.0 GA: Use MessageWindowChatMemory (InMemoryChatMemory is removed)
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean("routerChatClient")
    public ChatClient routerChatClient(ChatClient.Builder builder, AiAuditAdvisor auditAdvisor) {
        return builder.clone()
                .defaultAdvisors(auditAdvisor)
                .build();
    }

    @Bean("financialConciergeAgentClient")
    public ChatClient financialConciergeAgentClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            SafeGuardAdvisor safeGuardAdvisor,
            AiAuditAdvisor auditAdvisor
    ) {
        return builder.clone()
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().topK(4).similarityThreshold(0.65).build())
                                .build(),
                        safeGuardAdvisor,
                        auditAdvisor
                )
                .build();
    }
}
```

### 1.3 Externalized Prompt Templates (`classpath:prompts/*.st`)
Never hardcode multi-line prompts as Java string literals inside business services. Store prompts under `src/main/resources/prompts/*.st` and bind parameters via `.param("key", value)`.

---

## 2. Five Proven Agentic AI Workflow Patterns

Choose the simplest pattern that solves the business requirement:

```
1. Routing Workflow (Intent Classifier -> Specialist Agent)
   Input -> RouterAgent (.entity(IntentClassification.class))
              ├── FinancialConciergeAgent (RAG + Read-Only Profile Tools)
              ├── TransactionAgent (Balance + Staged HITL Transfer Tools)
              └── CardSecurityAgent (Staged HITL Card Lock/Unlock Tools)

2. Evaluator-Optimizer / Guardrail Loop
   Draft Response -> RiskAndPolicyEvaluatorAgent (.entity(PolicyEvaluation.class))
              ├── Pass -> Return / Stream to User
              └── Fail -> Refine with Evaluator Feedback (max 2 iterations)

3. Chain Workflow
   Step 1 Output -> Step 2 Input (Sequential transformation)

4. Parallelization Workflow
   CompletableFuture.allOf(sentimentTask, entityTask, policyCheckTask)

5. Orchestrator-Workers Workflow
   Planner LLM (.entity(TaskPlan.class)) -> Parallel Worker Agents -> Synthesizer LLM
```

### Multi-Agent Contract Pattern (SOLID / Strategy Pattern)
Represent each specialist agent as a dedicated Spring `@Component` implementing a common `SpecialistAiAgent` interface:

```java
public interface SpecialistAiAgent {
    AgentCategory supportedCategory();
    String execute(AgentExecutionContext context);
}
```

This satisfies the **Open/Closed Principle (OCP)** and **Single Responsibility Principle (SRP)**: adding a new specialist agent (e.g., `LoanAdvisorAgent` or `FraudDisputeAgent`) requires zero changes to the orchestrator service.

---

## 3. RAG & Dual-Tier Memory Architecture

1. **Short-Term Conversational Working Memory**:
   - Pass user/session-scoped `ChatMemory.CONVERSATION_ID` to `MessageChatMemoryAdvisor`:
     ```java
     .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId + ":" + sessionId))
     ```
   - Mirror turns in MongoDB (`ai_memory_db.chat_turns`) using `Criteria` queries for auditability and cross-restart continuity.
2. **Long-Term Semantic RAG (`VectorStore` + `QuestionAnswerAdvisor`)**:
   - Seed domain policies, FAQs, and user financial goals as `org.springframework.ai.document.Document` chunks with metadata (`category`, `docId`, `updatedAt`).
   - Attach `QuestionAnswerAdvisor.builder(vectorStore)` (or `RetrievalAugmentationAdvisor`) to inject grounded context automatically before the LLM call.

---

## 4. OWASP LLM Top 10 (2025) Security & Excessive Agency Controls

| OWASP Risk | Mandatory Control in Spring AI |
| :--- | :--- |
| **LLM01: Prompt Injection** | Attach `SafeGuardAdvisor` to block jailbreak/injection phrases (`ignore previous instructions`, `reveal system prompt`, `bypass confirmation`) before reaching the model. |
| **LLM02 & LLM07: Sensitive / System Prompt Leakage** | Keep `spring.ai.chat.observations.log-prompt=false` in production; never pass raw credentials or full card PANs into prompt parameters. |
| **LLM05: Improper Output Handling** | Validate responses via `EvaluatorAgent` or structured `.entity(Record.class)` output; never execute raw SQL or shell commands from LLM text. |
| **LLM06: Excessive Agency** | **Least-Privilege Tool Segregation**: Do NOT give all tools to all agents! `ConciergeAgent` gets read-only profile/FAQ tools; `TransactionAgent` gets balance & transfer-staging tools; `SecurityAgent` gets card-staging tools. **Mandatory Human-in-the-Loop (HITL)** for all state-mutating tools (`transferFunds`, `updateCardStatus`). |
| **LLM10: Unbounded Consumption** | Pre-check **Redis Semantic Cache** (`semcache:{sha256}`) for FAQ queries; enforce token/latency tracking and gRPC deadlines. |

---

## 5. AI Observability (`CallAdvisor` + Micrometer GenAI Metrics)

In Spring AI 1.0 GA, `CallAroundAdvisor` is replaced by `CallAdvisor` (`ChatClientRequest` / `ChatClientResponse`), and `Usage.getGenerationTokens()` is replaced by `Usage.getCompletionTokens()`.

Always register an `AiAuditAdvisor implements CallAdvisor` and `AiObservabilityMetrics` bean recording:
- `ai.prompt.latency` (`Timer` tagged by `agent`, `operation`, `status`)
- `ai.tokens.used` (`Counter` tagged by `agent`, `type=input|output`)
- Structured `[AI-AUDIT]` log lines with `requestId`, `agent`, `latencyMs`, `inputTokens`, `outputTokens`.

---

## 6. Reference Files
- [Agentic Workflow Patterns](../spring-ai/references/agentic-patterns.md)
- [RAG & Advisors Guide](../spring-ai/references/rag-advisors.md)
- [OWASP LLM06 Excessive Agency](../llm-security/rules/excessive-agency.md)
- [Spring AI Observability](../ai-observability/SKILL.md)
