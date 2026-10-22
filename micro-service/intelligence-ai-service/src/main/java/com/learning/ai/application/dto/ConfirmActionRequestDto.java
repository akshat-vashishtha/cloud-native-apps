package com.learning.ai.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmActionRequestDto(
        @NotBlank(message = "Confirmation token is required")
        String confirmationToken
) {}
