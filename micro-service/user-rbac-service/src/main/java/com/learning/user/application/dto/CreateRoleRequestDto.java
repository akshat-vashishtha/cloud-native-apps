package com.learning.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateRoleRequestDto(
        @NotBlank(message = "Role name is required")
        String name,

        String description,

        @NotEmpty(message = "Permissions cannot be empty")
        Set<String> permissions
) {}
