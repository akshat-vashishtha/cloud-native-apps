# Service: Intelligence-Ai-Service
Status: draft   Owner: @Elena, @Leo (Reviewer: @Alex)   Updated: 2026-10-10
Inputs: context/architecture.md, context/implementation.md

## Summary
Low-Level Design (LLD) for `Intelligence-Ai-Service`—the AI Financial Concierge ("Sophia / Wealth AI"). Implements Spring AI 2.0 with GPT-4, Model Context Protocol (MCP) tool execution backed by gRPC stubs to `Business-Operation-Service`, Dual-Tier Chat Memory (MongoDB short-term working turns + Qdrant 1536-dimensional vector RAG), Human-in-the-Loop 2-Step verification tokens, and Redis semantic caching.

---

## Decisions

- **SVC-AI-001**: **Spring AI 2.0 ChatClient & Declarative MCP Tool Registry**
  - *Why*: Spring AI's `@Tool` annotations allow declarative function-calling schemas exposed to GPT-4. Tools invoke `Business-Operation-Service` over low-latency gRPC stubs.
  - *Rejected*: Custom JSON parsing or hardcoding execution logic inside prompt strings.

- **SVC-AI-002**: **Dual-Tier Memory Architecture (MongoDB + Qdrant)**
  - *Why*: Prevents context window bloat and runaway token costs. Short-term active working memory (last 10 turns) is retrieved from MongoDB (`ai_memory_db.chat_turns`); long-term episodic summaries and banking FAQs are retrieved via Qdrant vector similarity search (`banking_knowledge_base`, `user_chat_longterm_memory`).
  - *Rejected*: Injecting full multi-month conversation histories into the LLM context window.

- **SVC-AI-003**: **Human-in-the-Loop 2-Step Confirmation for Financial Mutations**
  - *Why*: Prevents unauthorized or hallucinated money transfers. When the LLM decides to execute `transferFunds()`, the service generates an HMAC-signed `ActionConfirmationToken` and returns a confirmation payload to the user over STOMP. The transfer executes only when the user signs the confirmation token.
  - *Rejected*: Auto-executing financial transfers directly from unverified LLM responses.

- **SVC-AI-004**: **Redis Semantic Cache Pre-Check**
  - *Why*: Identical or semantically matching prompt queries are served from Redis (`semcache:{sha256(prompt)}`) in $<1\text{ms}$, reducing OpenAI API costs and latency.
  - *Rejected*: Hitting the LLM for repetitive static queries.

---

## Details

### 1. Spring AI 2.0 Configuration & Advisor Pipeline

```java
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient financialChatClient(
            ChatClient.Builder builder,
            VectorStore qdrantVectorStore,
            MongoChatMemoryRepository mongoChatMemoryRepo,
            BankingMcpTools bankingTools
    ) {
        return builder
            .defaultSystem("""
                You are Sophia, an elite, secure AI Financial Concierge for our Digital Banking platform.
                You help customers check balances, analyze spending, understand banking policies, and initiate transfers.
                For financial transfers and card status updates, you MUST ask the user for explicit confirmation before final execution.
                Always maintain a professional, concise, and helpful tone.
                """)
            .defaultTools(bankingTools)
            // Advisor 1: Short-Term Working Memory (MongoDB)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(mongoChatMemoryRepo),
                // Advisor 2: RAG over Qdrant Vector Store (Policies & Past Financial Goals)
                new QuestionAnswerAdvisor(qdrantVectorStore, SearchRequest.builder().topK(3).similarityThreshold(0.75).build()),
                // Advisor 3: Logging & Telemetry
                new SimpleLoggerAdvisor()
            )
            .build();
    }
}
```

---

### 2. Declarative MCP Tools (Calling `Business-Operation-Service` via gRPC)

```java
@Component
public class BankingMcpTools {

    private final BankingGrpcServiceBlockingStub bankingGrpcStub;
    private final ConfirmationTokenService tokenService;

    public BankingMcpTools(BankingGrpcServiceBlockingStub bankingGrpcStub, ConfirmationTokenService tokenService) {
        this.bankingGrpcStub = bankingGrpcStub;
        this.tokenService = tokenService;
    }

    @Tool(description = "Get the current balance and status of a bank account")
    public AccountBalanceResult checkBalance(
            @ToolParam(description = "Account ID to query") String accountId,
            @ToolParam(description = "Authenticated User ID") String userId
    ) {
        try {
            GetBalanceResponse response = bankingGrpcStub.getAccountBalance(
                GetBalanceRequest.newBuilder()
                    .setAccountId(accountId)
                    .setUserId(userId)
                    .build()
            );
            return new AccountBalanceResult(response.getAccountId(), response.getBalance(), response.getCurrency(), response.getStatus());
        } catch (StatusRuntimeException ex) {
            return new AccountBalanceResult(accountId, 0.0, "USD", "ERROR: " + ex.getStatus().getDescription());
        }
    }

    @Tool(description = "Initiate a money transfer between accounts. Returns a 2-Step Confirmation Token that the user must approve.")
    public TransferInitiationResult requestTransfer(
            @ToolParam(description = "Source Account ID") String sourceAccountId,
            @ToolParam(description = "Destination Account ID") String targetAccountId,
            @ToolParam(description = "Amount to transfer") double amount,
            @ToolParam(description = "Currency code e.g. USD") String currency,
            @ToolParam(description = "Authenticated User ID") String userId
    ) {
        // Step 1: Generate signed 2-Step Confirmation Token (Valid for 3 minutes)
        String confirmationToken = tokenService.generateToken(userId, sourceAccountId, targetAccountId, amount, currency);

        return new TransferInitiationResult(
            "CONFIRMATION_REQUIRED",
            sourceAccountId,
            targetAccountId,
            amount,
            currency,
            confirmationToken,
            "Please confirm transferring " + amount + " " + currency + " to account " + targetAccountId
        );
    }
}
```

---

### 3. Human-in-the-Loop 2-Step Verification Token Service

```java
@Service
public class ConfirmationTokenService {

    @Value("${app.security.token-secret}")
    private String tokenSecret;

    public String generateToken(String userId, String sourceAcc, String targetAcc, double amount, String currency) {
        long expiresAt = Instant.now().plus(Duration.ofMinutes(3)).getEpochSecond();
        String payload = String.format("%s:%s:%s:%.2f:%s:%d", userId, sourceAcc, targetAcc, amount, currency, expiresAt);
        String signature = HmacUtils.hmacSha256Hex(tokenSecret, payload);
        return Base64.getUrlEncoder().encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public TransferCommand validateAndExtract(String tokenString, String currentUserId) {
        String decoded = new String(Base64.getUrlDecoder().decode(tokenString), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":");
        if (parts.length != 7) throw new InvalidTokenException("Malformed confirmation token");

        String userId = parts[0];
        String sourceAcc = parts[1];
        String targetAcc = parts[2];
        double amount = Double.parseDouble(parts[3]);
        String currency = parts[4];
        long expiresAt = Long.parseLong(parts[5]);
        String signature = parts[6];

        // Replay and Expiry Check
        if (Instant.now().getEpochSecond() > expiresAt) throw new TokenExpiredException("Confirmation token has expired");
        if (!userId.equals(currentUserId)) throw new SecurityException("User mismatch");

        // Verify HMAC
        String expectedPayload = String.format("%s:%s:%s:%.2f:%s:%d", userId, sourceAcc, targetAcc, amount, currency, expiresAt);
        if (!HmacUtils.hmacSha256Hex(tokenSecret, expectedPayload).equals(signature)) {
            throw new SecurityException("Invalid token signature");
        }

        return new TransferCommand(sourceAcc, targetAcc, BigDecimal.valueOf(amount), currency, "AI Confirmed Transfer");
    }
}
```

---

### 4. Dual-Tier Chat Memory Architecture

#### Tier 1: MongoDB Working Memory (`ai_memory_db.chat_turns`)
```java
@Document(collection = "chat_turns")
@CompoundIndex(name = "user_session_idx", def = "{'userId': 1, 'sessionId': 1, 'timestamp': -1}")
public class ChatTurnDocument {
    @Id private String id;
    private String userId;
    private String sessionId;
    private String messageType; // "USER", "ASSISTANT", "TOOL"
    private String content;
    private Instant timestamp;
}
```

#### Tier 2: Qdrant Vector Store Collections
```java
@Configuration
public class QdrantVectorStoreConfig {

    @Bean
    public VectorStore qdrantVectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
            .collectionName("banking_knowledge_base")
            .initializeSchema(true)
            .build();
    }
}
```

---

### 5. Reactive Live STOMP Stream Controller

```java
@Controller
public class AiChatStompController {

    private final ChatClient financialChatClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConfirmationTokenService tokenService;
    private final BankingGrpcServiceBlockingStub bankingGrpcStub;

    // 1. Live Chat Stream
    @MessageMapping("/ai/chat")
    public void handleUserPrompt(@Payload UserChatPromptDto promptDto, Principal principal) {
        String userId = principal.getName();
        String destination = "/user/" + userId + "/queue/ai-stream";

        financialChatClient.prompt()
            .user(promptDto.message())
            .advisors(advisorSpec -> advisorSpec.param("chat_memory_conversation_id", promptDto.sessionId()))
            .stream()
            .content()
            .subscribe(token -> {
                messagingTemplate.convertAndSend(destination, new StreamTokenDto(token, promptDto.messageId()));
            });
    }

    // 2. User Confirms 2-Step Transfer
    @MessageMapping("/ai/confirm-transfer")
    public void confirmTransfer(@Payload ConfirmActionDto confirmDto, Principal principal) {
        String userId = principal.getName();
        TransferCommand cmd = tokenService.validateAndExtract(confirmDto.token(), userId);

        // Execute gRPC transfer against Business-Operation-Service
        TransferFundsResponse response = bankingGrpcStub.transferFunds(
            TransferFundsRequest.newBuilder()
                .setSourceAccountId(cmd.sourceAccountId())
                .setTargetAccountId(cmd.targetAccountId())
                .setAmount(cmd.amount().doubleValue())
                .setCurrency(cmd.currency())
                .setUserId(userId)
                .build()
        );

        messagingTemplate.convertAndSend("/user/" + userId + "/queue/ai-stream",
            new StreamTokenDto("Transfer of $" + cmd.amount() + " completed successfully! Tx ID: " + response.getTransactionId(), confirmDto.messageId()));
    }
}
```

---

## Plan
Approved: no

- [ ] 1. Spring AI 2.0 dependencies (`spring-ai-openai-spring-boot-starter`, `spring-ai-qdrant-store-spring-boot-starter`)
- [ ] 2. ChatClient Configuration with Advisors (`MessageChatMemoryAdvisor`, `QuestionAnswerAdvisor`)
- [ ] 3. Declarative MCP Tools with gRPC stub bindings to `Business-Operation-Service`
- [ ] 4. HMAC 2-Step Confirmation Token Generator & Validator
- [ ] 5. MongoDB Short-Term Memory Repository (`ai_memory_db.chat_turns`)
- [ ] 6. Qdrant Vector Store Collection initializers & Embeddings pipeline
- [ ] 7. STOMP WebSocket Live Token Stream Controller (`/user/queue/ai-stream`)
- [ ] 8. Unit tests with Mockito & Integration tests with Testcontainers Qdrant/MongoDB

## Code Progress
- Not started.

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Review and discuss the LLD for `Intelligence-Ai-Service`.
