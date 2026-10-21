package com.learning.chat.application.dto;

import java.time.Instant;

public record UserPresenceDto(
        String userId,
        String status, // "ONLINE" or "OFFLINE"
        Instant timestamp
) {
    public static UserPresenceDto online(String userId) {
        return new UserPresenceDto(userId, "ONLINE", Instant.now());
    }

    public static UserPresenceDto offline(String userId) {
        return new UserPresenceDto(userId, "OFFLINE", Instant.now());
    }
}
