package com.learning.gateway.application.dto;

import java.util.List;

public record ValidatedClaims(
        String userId,
        String email,
        List<String> roles,
        List<String> permissions
) {}