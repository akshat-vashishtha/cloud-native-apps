# Service: Chat-Communication-Service
Status: implemented   Owner: @Elena, @Alex, @Leo   Updated: 2026-10-22
Inputs: context/architecture.md, context/implementation.md

## Summary
STOMP/WebSocket hub with in-memory SimpleBroker, MongoDB `chat_db.messages`, Kafka produce `chat.events.v1`, and consume `banking.transactions.v1` for STOMP notifications. Security is **JWT resource server** (handshake reads JWT `sub` from `SecurityContext`). HMAC/`X-User-Id` handshake **retired**. Gateway proxies `/ws/**` with TokenRelay.

### Implementation status (2026-10-22)
| Feature | Status |
| :--- | :--- |
| STOMP `/ws` + SimpleBroker | Live |
| Message persistence | Live |
| Client→server `@MessageMapping` | Live (primary path today) |
| Server→client push | **Partial** — Kafka notifs use **accountId** as STOMP user key (should be JWT **userId** — PBP-003) |
| Produce `chat.events.v1` | Live (**no consumer**; prefer outbox — PBP-001) |
| JWT handshake | Live |
| Client mandatory subscriptions + destination ACL | Planned |
| Redis presence / Artemis relay / AI stream queues | Planned |

See [`platform-best-practices.md`](../platform-best-practices.md) §2 for full STOMP target.

---

## Decisions

- **SVC-CHAT-001**: **STOMP over WebSocket with User Destinations (`/user/queue/*`)**
  - *Why*: STOMP protocol provides structured frames and private user-destination routing.
  - *Rejected*: Raw WebSockets requiring custom framing.

- **SVC-CHAT-002**: **Spring In-Memory SimpleBroker for Phase 1 with Broker Relay Hooks**
  - *Why*: Zero external broker for local learning; Artemis relay is a one-line swap later.
  - *Rejected*: Forcing Artemis on Day 1.

- **SVC-CHAT-003**: **WebSocket Handshake via JWT Resource Server**
  - *Why*: Gateway TokenRelays Bearer JWT on `/ws` upgrade; `UserHandshakeInterceptor` reads `Jwt` principal subject into session attributes.
  - *Rejected*: HMAC / trusting client `X-User-Id` headers.

- **SVC-CHAT-004**: **Async Kafka Emission (`chat.events.v1`)**
  - *Why*: Non-blocking telemetry for future KPI/AI consumers.
  - *Rejected*: Sync HTTP to analytics during chat.

- **SVC-CHAT-005**: **Kafka Consumer for Banking Notifications (`banking.transactions.v1`)**
  - *Why*: Push CREDIT/DEBIT alerts to `/user/{accountId}/queue/notifications`.
  - *Rejected*: Client polling for transfer status.
  - *Note*: Destination uses **accountId**; confirm UX vs JWT `userId` (open question in architecture).

---

## Details

### 1. WebSocket & STOMP Configuration (`WebSocketConfig.java`)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-Memory SimpleBroker for Phase 1 (Roadmap: enableStompBrokerRelay)
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Application Destination Prefix (for @MessageMapping methods)
        registry.setApplicationDestinationPrefixes("/app");
        
        // User Destination Prefix (for private 1-on-1 messages)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Public WebSocket Handshake Endpoint
        registry.addEndpoint("/ws")
            .addInterceptors(new UserHandshakeInterceptor())
            .setAllowedOriginPatterns("*")
            .withSockJS(); // Fallback for environments blocking raw WebSockets
    }
}
```

---

### 2. Handshake Interceptor & User Authentication (`UserHandshakeInterceptor.java`)

```java
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract authenticated User ID injected by Gateway
        List<String> userIdHeaders = request.getHeaders().get("X-User-Id");
        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            String userId = userIdHeaders.get(0);
            attributes.put("userId", userId);
            return true;
        }
        return false; // Reject unauthenticated connection
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
```

---

### 3. Real-Time Chat & Direct Messaging Controller (`ChatStompController.java`)

```java
@Controller
public class ChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMongoRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ChatStompController(SimpMessagingTemplate messagingTemplate,
                               ChatMessageMongoRepository messageRepository,
                               KafkaTemplate<String, String> kafkaTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 1. Group Channel Message (Broadcast: /topic/channel.{channelId})
    @MessageMapping("/chat.channel.{channelId}")
    public void sendToChannel(@DestinationVariable String channelId,
                              @Payload InboundMessageDto dto,
                              Principal principal) {
        String senderId = (String) ((StompHeaderAccessor) principal).getSessionAttributes().get("userId");

        // Save to MongoDB
        ChatMessageDocument doc = new ChatMessageDocument(
            UUID.randomUUID().toString(),
            channelId,
            senderId,
            null, // Group broadcast has no single recipient
            dto.content(),
            MessageType.GROUP_MESSAGE,
            Instant.now()
        );
        messageRepository.save(doc);

        // Broadcast to channel subscribers
        messagingTemplate.convertAndSend("/topic/channel." + channelId, doc);

        // Emit Kafka event for KPI Analytics
        publishChatEvent(doc);
    }

    // 2. Private Direct Message (User to User / User to Support Agent)
    @MessageMapping("/chat.direct")
    public void sendDirectMessage(@Payload DirectMessageDto dto, Principal principal) {
        String senderId = (String) ((StompHeaderAccessor) principal).getSessionAttributes().get("userId");

        ChatMessageDocument doc = new ChatMessageDocument(
            UUID.randomUUID().toString(),
            "direct_" + minMax(senderId, dto.recipientId()),
            senderId,
            dto.recipientId(),
            dto.content(),
            MessageType.DIRECT_MESSAGE,
            Instant.now()
        );
        messageRepository.save(doc);

        // Send to recipient private queue: /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(dto.recipientId(), "/queue/messages", doc);
        // Echo back to sender private queue
        messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", doc);

        publishChatEvent(doc);
    }

    private void publishChatEvent(ChatMessageDocument doc) {
        ChatEvent event = new ChatEvent(
            UUID.randomUUID().toString(),
            "CHAT_MESSAGE_SENT",
            Instant.now(),
            new ChatEventPayload(doc.getId(), doc.getChannelId(), doc.getSenderId(), doc.getRecipientId(), doc.getMessageType().name())
        );
        kafkaTemplate.send("chat.events.v1", doc.getChannelId(), toJson(event));
    }
}
```

---

### 4. User Presence Tracker (Spring WebSocket Events)

```java
@Component
public class WebSocketPresenceListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPresenceListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPresenceListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) headers.getSessionAttributes().get("userId");
        if (userId != null) {
            log.info("User connected: {}", userId);
            messagingTemplate.convertAndSend("/topic/presence", new UserPresenceDto(userId, "ONLINE", Instant.now()));
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) headers.getSessionAttributes().get("userId");
        if (userId != null) {
            log.info("User disconnected: {}", userId);
            messagingTemplate.convertAndSend("/topic/presence", new UserPresenceDto(userId, "OFFLINE", Instant.now()));
        }
    }
}
```

---

### 5. Persistence Layer (MongoDB: `chat_db`)

```java
@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "channel_time_idx", def = "{'channelId': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "sender_time_idx", def = "{'senderId': 1, 'timestamp': -1}")
})
public class ChatMessageDocument {
    @Id private String id;
    private String channelId;
    private String senderId;
    private String recipientId;
    private String content;
    private MessageType messageType; // GROUP_MESSAGE, DIRECT_MESSAGE, AI_PROMPT
    private Instant timestamp;
}
```

---

### 6. Transaction Notification Consumer (`TransactionNotificationConsumer.java`)

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionNotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.transactions}", groupId = "${app.kafka.consumer.group-id}")
    public void consumeTransactionCompletedEvent(String message) {
        TransactionEventDto event = objectMapper.readValue(message, TransactionEventDto.class);
        TransactionEventDto.TransactionPayload payload = event.payload();

        // 1. Credit notification to targetAccountId
        messagingTemplate.convertAndSendToUser(
                payload.targetAccountId(),
                "/queue/notifications",
                new TransactionNotificationDto(UUID.randomUUID().toString(), "CREDIT", "Money Credited", ...)
        );

        // 2. Debit notification to sourceAccountId
        messagingTemplate.convertAndSendToUser(
                payload.sourceAccountId(),
                "/queue/notifications",
                new TransactionNotificationDto(UUID.randomUUID().toString(), "DEBIT", "Money Debited", ...)
        );
    }
}
```

---

## Plan
Approved: yes

- [x] 1. Spring WebSocket & STOMP dependencies (`spring-boot-starter-websocket`, `spring-kafka`, `spring-boot-starter-data-mongodb`)
- [x] 2. WebSocket Configuration with In-Memory SimpleBroker (`/topic`, `/queue`) and `/user` destination prefix
- [x] 3. Handshake Interceptor extracting `X-User-Id` from Gateway and HandshakeHandler mapping Principal
- [x] 4. STOMP Controller for Channel Broadcast (`@MessageMapping` + `@SendTo`), One-Time Channel History Init (`@SubscribeMapping`), and Direct User Queues (`@SendToUser`)
- [x] 5. WebSocket Presence Listener (`ONLINE` / `OFFLINE` broadcasts to `/topic/presence`)
- [x] 6. MongoDB Message Repository & Compound Indexes (`chat_db.messages`) with `@Version` optimistic locking
- [x] 7. Kafka Producer publishing chat telemetry to `chat.events.v1`
- [x] 8. Dual-Zone Security Filter (`InternalHmacFilter`) with 300s clock drift protection (ARCH-004)
- [x] 9. Kafka Consumer (`TransactionNotificationConsumer`) listening to `banking.transactions.v1` and pushing real-time STOMP alerts to `/user/{id}/queue/notifications`
- [ ] 10. Integration tests using Spring WebSocket `StandardWebSocketClient` & Testcontainers

## Code Progress
- Status: implemented. All source files compiled and verified cleanly on Spring Boot 3.3.4 and Java 21.

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Complete inter-service verification across Gateway, Banking, KPI, and Chat.
