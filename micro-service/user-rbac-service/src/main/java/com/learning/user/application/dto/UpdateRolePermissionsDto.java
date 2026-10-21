package com.learning.user.application.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolePermissionsDto(
        @NotEmpty(message = "Permissions cannot be empty")
        Set<String> permissions
) {}
