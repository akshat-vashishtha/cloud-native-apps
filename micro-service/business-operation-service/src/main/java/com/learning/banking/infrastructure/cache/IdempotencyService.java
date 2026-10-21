package com.learning.banking.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.banking.application.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String KEY_PREFIX = "idempotency:transfer:";
    private static final String PROCESSING_STATUS = "PROCESSING";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean acquireLock(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        Boolean accquired = redisTemplate.opsForValue().setIfAbsent(key, PROCESSING_STATUS, LOCK_TIMEOUT);
        return Boolean.TRUE.equals(accquired);
    }

    public void releaseLock(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(key);
        log.info("Released idempotency lock for key: {}", idempotencyKey);
    }

    public void saveResponse(String idempotencyKey, TransferResponse transferResponse) {
        String key = KEY_PREFIX + idempotencyKey;
        try {
            String json = objectMapper.writeValueAsString(transferResponse);
            redisTemplate.opsForValue().set(key, json, COMPLETED_TTL);
            log.info("Cached successful idempotency response for key: {}", idempotencyKey);
        } catch (Exception exception) {
            log.error("Failed to serialize TransferResponse for key: {}", idempotencyKey, exception);
        }
    }

    public Optional<TransferResponse> getResponse(String idempotencyKey) {
        String key = KEY_PREFIX + idempotencyKey;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null || PROCESSING_STATUS.equals(value)) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, TransferResponse.class));
        } catch (Exception exception) {
            log.error("Failed to deserialize cached TransferResponse for key: {}", idempotencyKey, exception);
            return Optional.empty();
        }
    }

}
