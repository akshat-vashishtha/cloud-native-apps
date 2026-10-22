package com.learning.chat.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.chat.application.dto.AiConfirmationEventDto;
import com.learning.chat.application.dto.AiStreamEventDto;
import com.learning.chat.application.dto.TransactionEventDto;
import com.learning.chat.application.dto.TransactionNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionNotificationConsumer {

    private static final String EVENT_DEDUPE_PREFIX = "event:dedupe:chat:";

    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.transactions}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void consumeTransactionCompletedEvent(String message) throws Exception {
        TransactionEventDto event = objectMapper.readValue(message, TransactionEventDto.class);
        if (event == null || event.payload() == null) {
            return;
        }
        if (event.eventId() != null && !tryAcquireEventDedupe(event.eventId())) {
            log.info("Skipping duplicate transaction notification event [{}]", event.eventId());
            return;
        }

        TransactionEventDto.TransactionPayload payload = event.payload();
        String status = payload.status() != null ? payload.status() : "SUCCESS";
        if (!"SUCCESS".equalsIgnoreCase(status)) {
            return;
        }

        String recipientUserKey = payload.targetUserId() != null && !payload.targetUserId().isBlank()
                ? payload.targetUserId()
                : payload.targetAccountId();
        if (recipientUserKey != null && !recipientUserKey.isBlank()) {
            String creditMsg = String.format("Aapke account me %s %s credit huye hain (Ref: %s)",
                    payload.amount(), payload.currency(), payload.description() != null ? payload.description() : "Transfer");

            TransactionNotificationDto creditNotification = new TransactionNotificationDto(
                    UUID.randomUUID().toString(),
                    "CREDIT",
                    "Money Credited",
                    creditMsg,
                    payload.amount(),
                    payload.currency(),
                    payload.transactionId(),
                    payload.targetAccountId(),
                    payload.sourceAccountId(),
                    Instant.now()
            );

            messagingTemplate.convertAndSendToUser(
                    recipientUserKey,
                    "/queue/notifications",
                    creditNotification
            );
        }

        String senderUserKey = payload.sourceUserId() != null && !payload.sourceUserId().isBlank()
                ? payload.sourceUserId()
                : payload.sourceAccountId();
        if (senderUserKey != null && !senderUserKey.isBlank()) {
            String debitMsg = String.format("Aapke account se %s %s debit huye hain (Ref: %s)",
                    payload.amount(), payload.currency(), payload.description() != null ? payload.description() : "Transfer");

            TransactionNotificationDto debitNotification = new TransactionNotificationDto(
                    UUID.randomUUID().toString(),
                    "DEBIT",
                    "Money Debited",
                    debitMsg,
                    payload.amount(),
                    payload.currency(),
                    payload.transactionId(),
                    payload.sourceAccountId(),
                    payload.targetAccountId(),
                    Instant.now()
            );

            messagingTemplate.convertAndSendToUser(
                    senderUserKey,
                    "/queue/notifications",
                    debitNotification
            );
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.ai-stream}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void consumeAiStreamEvent(String message) throws Exception {
        AiStreamEventDto streamEvent = objectMapper.readValue(message, AiStreamEventDto.class);
        if (streamEvent == null || streamEvent.userId() == null || streamEvent.userId().isBlank()) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                streamEvent.userId(),
                "/queue/ai-stream",
                streamEvent
        );
    }

    @KafkaListener(
            topics = "${app.kafka.topics.ai-confirmations}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void consumeAiConfirmationEvent(String message) throws Exception {
        AiConfirmationEventDto confirmationEvent = objectMapper.readValue(message, AiConfirmationEventDto.class);
        if (confirmationEvent == null || confirmationEvent.userId() == null || confirmationEvent.userId().isBlank()) {
            return;
        }
        if (confirmationEvent.confirmationToken() != null
                && !tryAcquireEventDedupe("confirm:" + confirmationEvent.confirmationToken())) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                confirmationEvent.userId(),
                "/queue/confirmations",
                confirmationEvent
        );
    }

    private boolean tryAcquireEventDedupe(String eventId) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(EVENT_DEDUPE_PREFIX + eventId, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(acquired);
    }
}
