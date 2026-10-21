package com.learning.chat.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.chat.application.dto.TransactionEventDto;
import com.learning.chat.application.dto.TransactionNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka Consumer that listens to banking transaction events and delivers
 * real-time WebSocket notifications to affected users via STOMP user destinations.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionNotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.transactions}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void consumeTransactionCompletedEvent(String message) {
        log.info("Kafka consumer received transaction event: {}", message);

        try {
            TransactionEventDto event = objectMapper.readValue(message, TransactionEventDto.class);
            if (event == null || event.payload() == null) {
                log.warn("Received empty or invalid transaction event payload, skipping");
                return;
            }

            TransactionEventDto.TransactionPayload payload = event.payload();
            String status = payload.status() != null ? payload.status() : "SUCCESS";

            if (!"SUCCESS".equalsIgnoreCase(status)) {
                log.info("Transaction [{}] status is [{}], skipping notification delivery",
                        payload.transactionId(), status);
                return;
            }

            // 1. Deliver CREDIT notification to recipient (Target Account)
            if (payload.targetAccountId() != null && !payload.targetAccountId().isBlank()) {
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

                log.info("Delivering CREDIT STOMP notification to user destination: /user/{}/queue/notifications",
                        payload.targetAccountId());
                messagingTemplate.convertAndSendToUser(
                        payload.targetAccountId(),
                        "/queue/notifications",
                        creditNotification
                );
            }

            // 2. Deliver DEBIT notification to sender (Source Account)
            if (payload.sourceAccountId() != null && !payload.sourceAccountId().isBlank()) {
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

                log.info("Delivering DEBIT STOMP notification to user destination: /user/{}/queue/notifications",
                        payload.sourceAccountId());
                messagingTemplate.convertAndSendToUser(
                        payload.sourceAccountId(),
                        "/queue/notifications",
                        debitNotification
                );
            }

        } catch (Exception e) {
            log.error("Failed to process transaction notification from Kafka message: {}", message, e);
        }
    }
}
