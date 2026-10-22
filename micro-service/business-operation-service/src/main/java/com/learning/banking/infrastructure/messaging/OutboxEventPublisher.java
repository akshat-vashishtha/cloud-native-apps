package com.learning.banking.infrastructure.messaging;

import com.learning.banking.infrastructure.config.BankingKafkaProperties;
import com.learning.banking.infrastructure.persistence.criteria.BankingCriteriaQueryAdapter;
import com.learning.banking.infrastructure.persistence.document.OutboxEventDocument;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final int MAX_RETRY_COUNT = 5;
    private static final int MAX_BATCH_SIZE = 50;

    private final BankingCriteriaQueryAdapter criteriaQueryAdapter;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BankingKafkaProperties kafkaProperties;

    public OutboxEventPublisher(
            BankingCriteriaQueryAdapter criteriaQueryAdapter,
            KafkaTemplate<String, String> kafkaTemplate,
            BankingKafkaProperties kafkaProperties
    ) {
        this.criteriaQueryAdapter = criteriaQueryAdapter;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            Optional<OutboxEventDocument> claimedOpt = criteriaQueryAdapter.claimNextPendingOutboxEvent();
            if (claimedOpt.isEmpty()) {
                return;
            }
            OutboxEventDocument event = claimedOpt.get();
            publishSingleClaimedEvent(event);
        }
    }

    private void publishSingleClaimedEvent(OutboxEventDocument event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    kafkaProperties.topics().transactions(),
                    event.getAggregateId(),
                    event.getPayload()
            );
            record.headers().add("eventId", event.getId().getBytes(StandardCharsets.UTF_8));
            record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));

            SendResult<String, String> result = kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
            RecordMetadata metadata = result.getRecordMetadata();

            criteriaQueryAdapter.markOutboxPublished(event.getId());

            log.info("Published claimed outbox event [ID: {}] to topic: {} | partition: {} | offset: {}",
                    event.getId(), metadata.topic(), metadata.partition(), metadata.offset());
        } catch (Exception exception) {
            int nextRetry = event.getRetryCount() + 1;
            criteriaQueryAdapter.markOutboxRetryOrFailed(event.getId(), nextRetry, MAX_RETRY_COUNT);
            log.warn("Failed to publish outbox event [ID: {}] on attempt {}/{}",
                    event.getId(), nextRetry, MAX_RETRY_COUNT, exception);
        }
    }
}
