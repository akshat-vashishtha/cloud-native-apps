package com.learning.oauth2.infrastructure.client;

import java.util.List;
import java.util.Set;

public record UserAuthDetailsResponse(
        String userId,
        String email,
        String fullName,
        String passwordHash,
        List<String> roles,
        Set<String> permissions,
        String status
) {
}
