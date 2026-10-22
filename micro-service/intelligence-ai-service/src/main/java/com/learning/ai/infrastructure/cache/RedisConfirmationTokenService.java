package com.learning.ai.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.ai.domain.model.PendingConfirmation;
import com.learning.ai.infrastructure.config.AiServiceProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisConfirmationTokenService {

    private static final String CONFIRMATION_KEY_PREFIX = "ai:confirmation:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AiServiceProperties properties;

    public RedisConfirmationTokenService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            AiServiceProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void savePendingConfirmation(PendingConfirmation confirmation) {
        try {
            String key = CONFIRMATION_KEY_PREFIX + confirmation.userId() + ":" + confirmation.confirmationToken();
            String json = objectMapper.writeValueAsString(confirmation);
            redisTemplate.opsForValue().set(
                    key,
                    json,
                    Duration.ofMinutes(properties.confirmationTtlMinutes())
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to persist HITL confirmation token", exception);
        }
    }

    public Optional<PendingConfirmation> consumePendingConfirmation(String token, String userId) {
        String key = CONFIRMATION_KEY_PREFIX + userId + ":" + token;
        String json = redisTemplate.opsForValue().getAndDelete(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            PendingConfirmation confirmation = objectMapper.readValue(json, PendingConfirmation.class);
            if (!confirmation.userId().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(confirmation);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
