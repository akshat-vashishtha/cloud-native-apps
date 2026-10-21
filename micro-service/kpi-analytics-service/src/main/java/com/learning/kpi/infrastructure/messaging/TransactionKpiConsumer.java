package com.learning.kpi.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.kpi.application.dto.FraudAlertData;
import com.learning.kpi.application.dto.TransactionEventData;
import com.learning.kpi.infrastructure.config.KpiKafkaProperties;
import com.learning.kpi.infrastructure.persistence.document.DailyMetricDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionKpiConsumer {

    public static final String VELOCITY_KEY_PREFIX = "velocity:";

    private final StringRedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KpiKafkaProperties kafkaProperties;

    @KafkaListener(topics = "${app.kafka.topics.transactions}", groupId = "${app.kafka.consumer.group-id}")
    public void onTransactionReceived(String message) {
        try {
            TransactionEventData event = objectMapper.readValue(message, TransactionEventData.class);
            if (event == null || event.payload() == null) {
                log.warn("Received empty transaction event payload, skipping");
                return;
            }

            TransactionEventData.TransactionPayload payload = event.payload();
            String accountId = payload.sourceAccountId();
            if (accountId == null || accountId.isBlank()) {
                log.warn("Transaction event without source account ID, skipping");
                return;
            }

            // 1. Sliding Fraud Velocity Tracking via Redis INCR
            checkAndTrackVelocity(accountId);

            // 2. Atomic Daily Aggregation Rollup in MongoDB
            recordDailyAggregation(payload);

        } catch (Exception e) {
            log.error("Failed to process transaction event from topic [{}]", kafkaProperties.topics().transactions(), e);
        }
    }

    private void checkAndTrackVelocity(String accountId) throws Exception {
        String velocityKey = VELOCITY_KEY_PREFIX + accountId;
        Long count = redisTemplate.opsForValue().increment(velocityKey);

        if (count != null && count == 1) {
            Duration windowDuration = Duration.ofMinutes(kafkaProperties.velocity().windowMinutes());
            redisTemplate.expire(velocityKey, windowDuration);
        }

        long threshold = kafkaProperties.velocity().threshold();
        if (count != null && count >= threshold) {
            log.warn("FRAUD ALERT: High velocity detected on account [{}] - {} transfers in {} mins",
                    accountId, count, kafkaProperties.velocity().windowMinutes());
            FraudAlertData alert = new FraudAlertData(
                    accountId,
                    "HIGH_VELOCITY_TRANSFERS",
                    "Account executed " + count + " transactions within " + kafkaProperties.velocity().windowMinutes() + " minutes",
                    count,
                    Instant.now()
            );
            kafkaTemplate.send(kafkaProperties.topics().fraudAlerts(), accountId, objectMapper.writeValueAsString(alert));
        }
    }

    private void recordDailyAggregation(TransactionEventData.TransactionPayload payload) {
        String currency = payload.currency() != null ? payload.currency() : "USD";
        double amount = payload.amount() != null ? payload.amount().doubleValue() : 0.0;
        String type = payload.transactionType() != null ? payload.transactionType() : "TRANSFER";
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        Query query = new Query(Criteria.where("currency").is(currency).and("windowStart").is(todayStart));
        Update update = new Update()
                .inc("totalVolume", amount)
                .inc("transactionCount", 1L)
                .inc("typeCounts." + type, 1L)
                .set("updatedAt", Instant.now())
                .setOnInsert("currency", currency)
                .setOnInsert("windowStart", todayStart);

        mongoTemplate.upsert(query, update, DailyMetricDocument.class);
        log.debug("Updated daily metric rollup for currency [{}]: amount={}, type={}", currency, amount, type);
    }
}
