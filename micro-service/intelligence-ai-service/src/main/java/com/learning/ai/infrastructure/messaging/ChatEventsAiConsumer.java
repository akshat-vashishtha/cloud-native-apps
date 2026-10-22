package com.learning.ai.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.ai.application.dto.AiChatRequestDto;
import com.learning.ai.application.service.AiConciergeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ChatEventsAiConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatEventsAiConsumer.class);
    private static final String EVENT_DEDUPE_PREFIX = "event:dedupe:ai:";

    private final AiConciergeService aiConciergeService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatEventsAiConsumer(
            AiConciergeService aiConciergeService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.aiConciergeService = aiConciergeService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.chat-events}", groupId = "${app.kafka.consumer.group-id}")
    public void onChatEvent(String payload) throws Exception {
        InboundChatEvent event = objectMapper.readValue(payload, InboundChatEvent.class);
        if (event == null || event.content() == null || event.senderId() == null) {
            return;
        }
        if (!"USER_PROMPT".equalsIgnoreCase(event.messageType())
                && (event.recipientId() == null || !"ai_assistant".equalsIgnoreCase(event.recipientId()))) {
            return;
        }
        if (event.id() != null && !tryDedupe(event.id())) {
            return;
        }

        log.info("AI Concierge triggered via Kafka chat.events.v1 for user [{}]", event.senderId());
        aiConciergeService.converse(
                event.senderId(),
                new AiChatRequestDto(event.content(), event.channelId())
        );
    }

    private boolean tryDedupe(String id) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(EVENT_DEDUPE_PREFIX + id, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(acquired);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundChatEvent(
            String id,
            String channelId,
            String senderId,
            String recipientId,
            String content,
            String messageType
    ) {}
}
