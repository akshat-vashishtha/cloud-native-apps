package com.learning.ai.domain.model;

import java.time.Instant;
import java.util.Objects;

public record ChatTurn(
        String id,
        String userId,
        String sessionId,
        String role,
        String content,
        Instant timestamp
) {
    public ChatTurn {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        Objects.requireNonNull(role, "role cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
    }
}
