package com.learning.chat.presentation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.chat.application.dto.ChatErrorResponseDto;
import com.learning.chat.application.dto.ChatHistoryResponseDto;
import com.learning.chat.application.dto.ChatMessageResponseDto;
import com.learning.chat.application.dto.DirectMessageDto;
import com.learning.chat.application.dto.InboundChatMessageDto;
import com.learning.chat.domain.enums.MessageType;
import com.learning.chat.domain.model.ChatMessage;
import com.learning.chat.infrastructure.config.ChatKafkaProperties;
import com.learning.chat.infrastructure.mapper.ChatMapper;
import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import com.learning.chat.infrastructure.persistence.repository.ChatMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageRepository messageRepository;
    private final ChatMapper chatMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ChatKafkaProperties kafkaProperties;

    // 1. Initial State on Subscribe (Fetch recent history when user enters room)
    @SubscribeMapping("/channel/{channelId}/history")
    public ChatHistoryResponseDto getInitialChannelHistory(@DestinationVariable String channelId) {
        log.info("Client subscribed to /channel/{}/history. Fetching initial state...", channelId);
        List<ChatMessageDocument> recentDocs = messageRepository.findTop50ByChannelIdOrderByTimestampDesc(channelId);

        // Reverse to ensure chronological order (oldest to newest)
        Collections.reverse(recentDocs);
        List<ChatMessageResponseDto> messages = recentDocs.stream()
                .map(chatMapper::toResponseDto)
                .toList();

        return new ChatHistoryResponseDto(channelId, messages.size(), messages);
    }

    // 2. Incoming Channel Message Broadcast
    @MessageMapping("/channel/{channelId}/send")
    @SendTo("/topic/channel.{channelId}")
    public ChatMessageResponseDto handleChannelMessage(
            @DestinationVariable String channelId,
            @Valid @Payload InboundChatMessageDto dto,
            @Header("simpSessionId") String sessionId,
            Principal principal) {

        String senderId = principal != null ? principal.getName() : "anonymous";
        log.info("Received channel message: channelId={}, senderId={}, sessionId={}", channelId, senderId, sessionId);

        String messageId = UUID.randomUUID().toString();
        ChatMessage chatMessage = new ChatMessage(
                messageId,
                channelId,
                senderId,
                null,
                dto.content(),
                dto.messageType() != null ? dto.messageType() : MessageType.GROUP_MESSAGE,
                Instant.now()
        );

        ChatMessageDocument savedDoc = messageRepository.save(chatMapper.toDocument(chatMessage));
        publishChatTelemetry(savedDoc);

        return chatMapper.toResponseDto(savedDoc);
    }

    // 3. Private 1-on-1 Direct Message
    @MessageMapping("/chat.direct")
    @SendToUser("/queue/reply")
    public ChatMessageResponseDto handleDirectMessage(
            @Valid @Payload DirectMessageDto dto,
            @Header("simpSessionId") String sessionId,
            Principal principal) {

        String senderId = principal != null ? principal.getName() : "anonymous";
        log.info("Direct message from {} to {}, sessionId={}", senderId, dto.recipientId(), sessionId);

        String messageId = UUID.randomUUID().toString();
        String channelId = "direct_" + (senderId.compareTo(dto.recipientId()) < 0
                ? senderId + "_" + dto.recipientId()
                : dto.recipientId() + "_" + senderId);

        ChatMessage chatMessage = ChatMessage.createDirectMessage(
                messageId,
                channelId,
                senderId,
                dto.recipientId(),
                dto.content()
        );

        ChatMessageDocument savedDoc = messageRepository.save(chatMapper.toDocument(chatMessage));
        ChatMessageResponseDto response = chatMapper.toResponseDto(savedDoc);

        // Deliver to recipient's private queue: /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(dto.recipientId(), "/queue/messages", response);

        publishChatTelemetry(savedDoc);

        return response;
    }

    // 4. Private Error / Query Validation
    @MessageMapping("/private-query")
    @SendToUser("/queue/errors")
    public ChatErrorResponseDto handlePrivateErrorQuery(
            @Payload String query,
            @Header("simpSessionId") String sessionId,
            Principal principal) {

        String userId = principal != null ? principal.getName() : "anonymous";
        log.warn("Private query syntax validation from userId={}, sessionId={}", userId, sessionId);

        return ChatErrorResponseDto.of("ERR_INVALID_SYNTAX", "Syntax validation error on query: " + query);
    }

    private void publishChatTelemetry(ChatMessageDocument doc) {
        if (kafkaTemplate != null) {
            try {
                String eventPayload = objectMapper.writeValueAsString(doc);
                kafkaTemplate.send(kafkaProperties.topics().chatEvents(), doc.getChannelId(), eventPayload)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.warn("Non-fatal: Failed to emit chat telemetry to Kafka for message {}", doc.getId());
                            }
                        });
            } catch (Exception e) {
                log.warn("Failed to serialize chat event for Kafka: {}", e.getMessage());
            }
        }
    }
}
