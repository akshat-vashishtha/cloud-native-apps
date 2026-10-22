package com.learning.chat.presentation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.chat.application.dto.ChatErrorResponseDto;
import com.learning.chat.application.dto.ChatHistoryResponseDto;
import com.learning.chat.application.dto.ChatMessageResponseDto;
import com.learning.chat.application.dto.DirectMessageDto;
import com.learning.chat.application.dto.InboundChatMessageDto;
import com.learning.chat.domain.enums.MessageType;
import com.learning.chat.domain.model.ChatMessage;
import com.learning.chat.infrastructure.mapper.ChatMapper;
import com.learning.chat.infrastructure.persistence.criteria.ChatCriteriaQueryAdapter;
import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import com.learning.chat.infrastructure.persistence.document.ChatOutboxEventDocument;
import com.learning.chat.infrastructure.persistence.repository.ChatMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageRepository messageRepository;
    private final ChatCriteriaQueryAdapter criteriaQueryAdapter;
    private final ChatMapper chatMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @SubscribeMapping("/channel/{channelId}/history")
    public ChatHistoryResponseDto getInitialChannelHistory(@DestinationVariable String channelId) {
        log.info("Client subscribed to /channel/{}/history. Fetching initial state via Criteria...", channelId);
        List<ChatMessageDocument> recentDocs = new ArrayList<>(
                criteriaQueryAdapter.findRecentMessagesByChannel(channelId, 50)
        );

        Collections.reverse(recentDocs);
        List<ChatMessageResponseDto> messages = recentDocs.stream()
                .map(chatMapper::toResponseDto)
                .toList();

        return new ChatHistoryResponseDto(channelId, messages.size(), messages);
    }

    @MessageMapping("/channel/{channelId}/send")
    @SendTo("/topic/channel.{channelId}")
    public ChatMessageResponseDto handleChannelMessage(
            @DestinationVariable String channelId,
            @Valid @Payload InboundChatMessageDto dto,
            @Header("simpSessionId") String sessionId,
            Principal principal) {

        String senderId = principal != null ? principal.getName() : "anonymous";
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
        enqueueChatOutboxEvent(savedDoc);

        return chatMapper.toResponseDto(savedDoc);
    }

    @MessageMapping("/chat.direct")
    @SendToUser("/queue/reply")
    public ChatMessageResponseDto handleDirectMessage(
            @Valid @Payload DirectMessageDto dto,
            @Header("simpSessionId") String sessionId,
            Principal principal) {

        String senderId = principal != null ? principal.getName() : "anonymous";
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

        messagingTemplate.convertAndSendToUser(dto.recipientId(), "/queue/messages", response);
        enqueueChatOutboxEvent(savedDoc);

        return response;
    }

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

    private void enqueueChatOutboxEvent(ChatMessageDocument doc) {
        try {
            String eventPayload = objectMapper.writeValueAsString(doc);
            ChatOutboxEventDocument outboxDoc = ChatOutboxEventDocument.builder()
                    .id(doc.getId())
                    .aggregateType("CHAT_MESSAGE")
                    .aggregateId(doc.getChannelId())
                    .eventType("CHAT_MESSAGE_SENT")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();
            criteriaQueryAdapter.saveOutboxEvent(outboxDoc);
        } catch (Exception e) {
            log.warn("Failed to enqueue chat outbox event for message {}: {}", doc.getId(), e.getMessage());
        }
    }
}
