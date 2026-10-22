package com.learning.chat.infrastructure.messaging;

import com.learning.chat.infrastructure.config.ChatKafkaProperties;
import com.learning.chat.infrastructure.persistence.criteria.ChatCriteriaQueryAdapter;
import com.learning.chat.infrastructure.persistence.document.ChatOutboxEventDocument;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ChatOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ChatOutboxEventPublisher.class);
    private static final int MAX_RETRIES = 5;
    private static final int MAX_BATCH_SIZE = 50;

    private final ChatCriteriaQueryAdapter criteriaQueryAdapter;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ChatKafkaProperties kafkaProperties;

    public ChatOutboxEventPublisher(
            ChatCriteriaQueryAdapter criteriaQueryAdapter,
            KafkaTemplate<String, String> kafkaTemplate,
            ChatKafkaProperties kafkaProperties
    ) {
        this.criteriaQueryAdapter = criteriaQueryAdapter;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingChatEvents() {
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            Optional<ChatOutboxEventDocument> claimedOpt = criteriaQueryAdapter.claimNextPendingOutboxEvent();
            if (claimedOpt.isEmpty()) {
                return;
            }
            ChatOutboxEventDocument event = claimedOpt.get();
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        kafkaProperties.topics().chatEvents(),
                        event.getAggregateId(),
                        event.getPayload()
                );
                record.headers().add("eventId", event.getId().getBytes(StandardCharsets.UTF_8));
                kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
                criteriaQueryAdapter.markOutboxPublished(event.getId());
                log.debug("Published chat outbox event [{}] to topic [{}]", event.getId(), kafkaProperties.topics().chatEvents());
            } catch (Exception ex) {
                int nextRetry = event.getRetryCount() + 1;
                criteriaQueryAdapter.markOutboxRetryOrFailed(event.getId(), nextRetry, MAX_RETRIES);
                log.warn("Failed to publish chat outbox event [{}], attempt {}/{}", event.getId(), nextRetry, MAX_RETRIES, ex);
            }
        }
    }
}
