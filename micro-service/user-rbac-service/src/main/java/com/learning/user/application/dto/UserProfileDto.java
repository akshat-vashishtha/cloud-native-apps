package com.learning.user.application.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record UserProfileDto(
        String id,
        String email,
        String fullName,
        List<String> roles,
        Set<String> permissions,
        String status
) implements Serializable {}
