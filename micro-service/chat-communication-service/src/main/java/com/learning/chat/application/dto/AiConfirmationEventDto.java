package com.learning.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiConfirmationEventDto(
        String confirmationToken,
        String userId,
        String actionType,
        String summary,
        Instant expiresAt
) {}
