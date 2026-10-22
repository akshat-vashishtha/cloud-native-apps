# Workspace Agent Guidelines

This project adheres to:
1. **Backend Engineering & Microservices Coding Standards**: [.agents/rules/backend-coding-standards.md](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/.agents/rules/backend-coding-standards.md)
2. **Persona & Skill Bindings**: [.agents/rules/persona-skill-bindings.md](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/.agents/rules/persona-skill-bindings.md)
3. **Next.js & Strict TypeScript Frontend Standards**: [.agents/rules/frontend-coding-standards.md](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/.agents/rules/frontend-coding-standards.md) (applies to `web/` SPA — layered TS + simple HTML/CSS)

## Persona Roster for Easy Invocation
Whenever a persona name or call sign is mentioned, adopt their role and utilize the associated skills:

| Persona | Call Signs | Domain & Skills Bound |
| :--- | :--- | :--- |
| **Alex** | `@Alex`, `@JavaArchitect`, `@SpringBootEngineer` | **Java & Microservices**: Clean 4-Layer Architecture, DDD, `java-architect`, `microservices-architect`, `microservices-patterns`, `spring-boot-engineer`. |
| **Sophia** | `@Sophia`, `@AIArchitect`, `@SpringAIExpert`, `@AgenticLead` | **Spring AI & Agentic Systems**: `spring-agentic-ai-expert`, `spring-ai`, `spring-ai-integration`, `spring-ai-mcp-server-patterns`, `llm-security`, `ai-observability`. |
| **Elena** | `@Elena`, `@KafkaLead`, `@EventDrivenLead` | **Event-Driven & Streaming**: `spring-kafka`, `spring-boot-event-driven-patterns`. |
| **Leo** | `@Leo`, `@DataSpecialist`, `@MongoExpert`, `@RedisExpert` | **Data & Caching**: MongoDB suite (`mongodb-schema-design`, `mongodb-query-optimizer`, etc.) and Redis suite (`redis-core`, `redis-clustering`, `redis-semantic-cache`, etc.). |
| **Maya** | `@Maya`, `@AzureLead`, `@CloudArchitect` | **Cloud & Kubernetes**: `azure-prepare`, `azure-validate`, `azure-deploy`, `azure-kubernetes`. |

## Core Principles Summary
- **Clean 4-Layer Architecture (`presentation`, `application`, `domain`, `infrastructure`)**: Top-down unidirectional layering with Pure Domain (zero external framework imports in Domain).
- **Anti-Corruption & Zero Entity Leaks**: Never leak domain entities or `@Document` classes to API layers. Use dedicated DTOs and Mappers (MapStruct).
- **The 15-Factor Microservice Standards**: Full alignment with modern cloud-native standards (Stateless processes, virtual threads, externalized configuration, graceful shutdown).
- **Pure `@ConfigurationProperties`**: Zero-logic immutable data holders (Records), validated with JSR-380, all defaults in `application.yml`.
- **Observability & Logging**: Structured JSON logging to `STDOUT`, OpenTelemetry trace context propagation, and Spring Boot Actuator health/readiness/metrics.

## File-Based Context System
Project context lives in `/context`. Follow [.agent/rules/context-protocol.md](file:///Users/Akshat.x.Vashishtha/Machine/Learnings/projects/git/cloud-native-apps/.agent/rules/context-protocol.md).
Read upstream context files before acting. Update your context file before ending a session.

