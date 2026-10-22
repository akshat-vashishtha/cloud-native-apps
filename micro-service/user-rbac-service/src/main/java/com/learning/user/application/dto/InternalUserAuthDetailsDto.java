package com.learning.user.application.dto;

import java.util.List;
import java.util.Set;

public record InternalUserAuthDetailsDto(
        String userId,
        String email,
        String fullName,
        String passwordHash,
        List<String> roles,
        Set<String> permissions,
        String status
) {
}
