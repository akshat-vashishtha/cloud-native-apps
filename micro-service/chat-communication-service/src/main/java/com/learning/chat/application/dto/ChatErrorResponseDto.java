package com.learning.chat.application.dto;

import java.time.Instant;

public record ChatErrorResponseDto(
        String errorCode,
        String errorMessage,
        Instant timestamp
) {
    public static ChatErrorResponseDto of(String errorCode, String errorMessage) {
        return new ChatErrorResponseDto(errorCode, errorMessage, Instant.now());
    }
}
