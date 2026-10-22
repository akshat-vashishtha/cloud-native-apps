package com.learning.ai.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.ai.domain.model.PendingConfirmation;
import com.learning.ai.infrastructure.config.AiKafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaAiStreamPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaAiStreamPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AiKafkaProperties kafkaProperties;

    public KafkaAiStreamPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            AiKafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProperties = kafkaProperties;
    }

    public void publishStreamChunk(String userId, String sessionId, String messageId, String token, boolean completed) {
        try {
            Map<String, Object> event = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "userId", userId,
                    "sessionId", sessionId,
                    "messageId", messageId,
                    "token", token,
                    "completed", completed,
                    "timestamp", Instant.now().toString()
            );
            kafkaTemplate.send(
                    kafkaProperties.topics().aiStream(),
                    userId,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception exception) {
            log.warn("Failed to publish AI stream chunk for user [{}]", userId, exception);
        }
    }

    public void publishConfirmationPrompt(PendingConfirmation confirmation) {
        try {
            Map<String, Object> event = Map.of(
                    "confirmationToken", confirmation.confirmationToken(),
                    "userId", confirmation.userId(),
                    "actionType", confirmation.actionType(),
                    "summary", confirmation.summary(),
                    "expiresAt", confirmation.expiresAt().toString()
            );
            kafkaTemplate.send(
                    kafkaProperties.topics().aiConfirmations(),
                    confirmation.userId(),
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception exception) {
            log.warn("Failed to publish AI HITL confirmation prompt for user [{}]", confirmation.userId(), exception);
        }
    }
}
