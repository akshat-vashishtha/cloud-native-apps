package com.learning.ai.infrastructure.cache;

import com.learning.ai.infrastructure.config.AiServiceProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RedisSemanticCacheService {

    private static final String SEMCACHE_PREFIX = "semcache:";

    private final StringRedisTemplate redisTemplate;
    private final AiServiceProperties properties;

    public RedisSemanticCacheService(StringRedisTemplate redisTemplate, AiServiceProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Optional<String> lookupCachedCompletion(String prompt) {
        String key = SEMCACHE_PREFIX + sha256Normalized(prompt);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void storeCompletion(String prompt, String response) {
        String key = SEMCACHE_PREFIX + sha256Normalized(prompt);
        redisTemplate.opsForValue().set(
                key,
                response,
                Duration.ofHours(properties.semanticCacheTtlHours())
        );
    }

    private String sha256Normalized(String prompt) {
        try {
            String normalized = prompt.trim().toLowerCase();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash prompt for semantic cache", exception);
        }
    }
}
