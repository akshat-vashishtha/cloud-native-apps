package com.learning.user.application.dto;

import java.util.Set;

public record RoleDto(
        String id,
        String name,
        String description,
        Set<String> permissions,
        boolean isSystemRole
) {}
