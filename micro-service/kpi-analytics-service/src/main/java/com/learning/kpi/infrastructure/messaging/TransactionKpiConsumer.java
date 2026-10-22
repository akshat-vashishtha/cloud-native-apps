package com.learning.kpi.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.kpi.application.dto.ChatEventData;
import com.learning.kpi.application.dto.FraudAlertData;
import com.learning.kpi.application.dto.TransactionEventData;
import com.learning.kpi.infrastructure.config.KpiKafkaProperties;
import com.learning.kpi.infrastructure.persistence.criteria.KpiCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionKpiConsumer {

    public static final String VELOCITY_KEY_PREFIX = "velocity:";
    private static final String EVENT_DEDUPE_KEY_PREFIX = "event:dedupe:kpi:";

    private static final DefaultRedisScript<Long> ATOMIC_INCR_WITH_TTL_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]); if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return c;",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final KpiCriteriaRepository kpiCriteriaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KpiKafkaProperties kafkaProperties;

    @KafkaListener(topics = "${app.kafka.topics.transactions}", groupId = "${app.kafka.consumer.group-id}")
    public void onTransactionReceived(String message) throws Exception {
        TransactionEventData event = objectMapper.readValue(message, TransactionEventData.class);
        if (event == null || event.payload() == null) {
            log.warn("Received empty transaction event payload, skipping");
            return;
        }

        if (event.eventId() != null && !tryAcquireEventDedupe(event.eventId())) {
            log.info("Skipping duplicate transaction event [{}] in KPI consumer", event.eventId());
            return;
        }

        TransactionEventData.TransactionPayload payload = event.payload();
        String accountId = payload.sourceAccountId();
        if (accountId == null || accountId.isBlank()) {
            log.warn("Transaction event without source account ID, skipping");
            return;
        }

        checkAndTrackVelocity(accountId);
        recordDailyAggregation(payload);
    }

    @KafkaListener(topics = "${app.kafka.topics.chat-events}", groupId = "${app.kafka.consumer.group-id}")
    public void onChatEventReceived(String message) throws Exception {
        ChatEventData chatEvent = objectMapper.readValue(message, ChatEventData.class);
        if (chatEvent == null) {
            return;
        }
        if (chatEvent.id() != null && !tryAcquireEventDedupe("chat:" + chatEvent.id())) {
            log.info("Skipping duplicate chat event [{}] in KPI consumer", chatEvent.id());
            return;
        }
        String type = chatEvent.messageType() != null ? chatEvent.messageType() : "GROUP_MESSAGE";
        kpiCriteriaRepository.recordDailyChatEngagementMetric(type);
        log.debug("Recorded chat telemetry metric for message [{}] type [{}]", chatEvent.id(), type);
    }

    private boolean tryAcquireEventDedupe(String eventId) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(EVENT_DEDUPE_KEY_PREFIX + eventId, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(acquired);
    }

    private void checkAndTrackVelocity(String accountId) throws Exception {
        String velocityKey = VELOCITY_KEY_PREFIX + accountId;
        long ttlSeconds = Duration.ofMinutes(kafkaProperties.velocity().windowMinutes()).toSeconds();
        Long count = redisTemplate.execute(
                ATOMIC_INCR_WITH_TTL_SCRIPT,
                List.of(velocityKey),
                String.valueOf(ttlSeconds)
        );

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
        kpiCriteriaRepository.recordDailyTransactionMetric(currency, amount, type);
    }
}
