package com.learning.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiStreamEventDto(
        String eventId,
        String userId,
        String sessionId,
        String messageId,
        String token,
        boolean completed,
        Instant timestamp
) {}
