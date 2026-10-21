package com.learning.banking.infrastructure.messaging;

import com.learning.banking.infrastructure.config.BankingKafkaProperties;
import com.learning.banking.infrastructure.persistence.document.OutboxEventDocument;
import com.learning.banking.infrastructure.persistence.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BankingKafkaProperties kafkaProperties;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                 KafkaTemplate<String, String> kafkaTemplate,
                                 BankingKafkaProperties kafkaProperties) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        List<OutboxEventDocument> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish to Kafka", pendingEvents.size());

        for (OutboxEventDocument event : pendingEvents) {
            try {
                // Synchronous wait ensures message is acked by Kafka before advancing,
                // preventing duplicate polling collisions on subsequent schedule ticks.
                SendResult<String, String> result = kafkaTemplate
                        .send(kafkaProperties.topics().transactions(), event.getAggregateId(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);

                RecordMetadata recordMetadata = result.getRecordMetadata();
                event.setStatus("PUBLISHED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                log.info("Successfully published outbox event [ID: {}] to Kafka topic: {} | Partition: {} | Offset: {}",
                        event.getId(),
                        recordMetadata.topic(),
                        recordMetadata.partition(),
                        recordMetadata.offset()
                );
            } catch (Exception exception) {
                int nextRetry = event.getRetryCount() + 1;
                event.setRetryCount(nextRetry);

                if (nextRetry >= MAX_RETRY_COUNT) {
                    event.setStatus("FAILED");
                    log.error("Outbox event [ID: {}] exceeded max retries ({}), marking as FAILED",
                            event.getId(), MAX_RETRY_COUNT, exception);
                } else {
                    log.warn("Failed to publish outbox event [ID: {}] on attempt {}/{}, will retry next cycle",
                            event.getId(), nextRetry, MAX_RETRY_COUNT, exception);
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
