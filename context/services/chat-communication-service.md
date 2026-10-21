# Service: Chat-Communication-Service
Status: draft   Owner: @Elena, @Alex, @Leo   Updated: 2026-10-10
Inputs: context/architecture.md, context/implementation.md

## Summary
Low-Level Design (LLD) for `Chat-Communication-Service`—the real-time bi-directional messaging and WebSocket streaming hub. Implements Spring WebSocket with STOMP protocol, an in-memory `SimpleBroker` (`/topic`, `/queue`), user presence tracking, chat message persistence in MongoDB (`chat_db`), and asynchronous event publishing to Kafka topic `chat.events.v1`.

---

## Decisions

- **SVC-CHAT-001**: **STOMP over WebSocket with User Destinations (`/user/queue/*`)**
  - *Why*: STOMP protocol provides structured frames (CONNECT, SUBSCRIBE, SEND, MESSAGE) and private user-destination routing (`/user/{userId}/queue/messages`, `/user/{userId}/queue/ai-stream`).
  - *Rejected*: Raw, unformatted WebSockets requiring custom application framing.

- **SVC-CHAT-002**: **Spring In-Memory SimpleBroker for Phase 1 with Broker Relay Hooks**
  - *Why*: In-memory `SimpleBroker` provides zero external dependency overhead during local development and learning. Configuration is structured so that switching to an external Apache ActiveMQ Artemis relay (`enableStompBrokerRelay`) requires only a one-line config swap.
  - *Rejected*: Forcing complex multi-pod message broker infrastructure on Day 1.

- **SVC-CHAT-003**: **WebSocket Handshake Auth via Gateway Header Propagation**
  - *Why*: The Gateway validates the `APP_SESSION` cookie during the initial HTTP `/ws` handshake upgrade and injects `X-User-Id` into WebSocket session attributes via `HandshakeInterceptor`.
  - *Rejected*: Re-authenticating credentials on every STOMP frame.

- **SVC-CHAT-004**: **Async Kafka Event Emission (`chat.events.v1`) for Analytics**
  - *Why*: Non-blocking publishing of `ChatEvent` to Kafka allows `KPI-Analytics-Service` to compute user engagement metrics without adding latency to real-time chat delivery.
  - *Rejected*: Synchronous HTTP calls to analytics services during chat flow.

- **SVC-CHAT-005**: **Async Kafka Consumer for Real-Time Banking Notifications (`banking.transactions.v1`)**
  - *Why*: Listens to completed transaction events emitted by `Business-Operation-Service` and pushes real-time CREDIT and DEBIT alerts to users' private STOMP queues (`/user/{accountId}/queue/notifications`) via `SimpMessagingTemplate`.
  - *Rejected*: Requiring mobile/web clients to poll for transaction status.

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
