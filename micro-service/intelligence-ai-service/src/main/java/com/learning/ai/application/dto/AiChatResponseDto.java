package com.learning.ai.application.dto;

import com.learning.ai.domain.enums.AgentCategory;

import java.time.Instant;

public record AiChatResponseDto(
        String messageId,
        String sessionId,
        AgentCategory routedAgent,
        String reply,
        boolean cachedHit,
        Instant timestamp
) {}
