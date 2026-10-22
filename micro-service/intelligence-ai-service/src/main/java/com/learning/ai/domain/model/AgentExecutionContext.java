package com.learning.ai.domain.model;

import com.learning.ai.domain.enums.AgentCategory;

import java.util.Objects;

public record AgentExecutionContext(
        String userId,
        String sessionId,
        String conversationId,
        String userPrompt,
        String memoryContext,
        AgentCategory category
) {
    public AgentExecutionContext {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        Objects.requireNonNull(userPrompt, "userPrompt cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
    }
}
