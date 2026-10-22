package com.learning.ai.application.dto;

import java.time.Instant;

public record ChatTurnResponseDto(
        String id,
        String userId,
        String sessionId,
        String role,
        String content,
        Instant timestamp
) {}
