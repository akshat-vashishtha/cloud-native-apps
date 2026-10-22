package com.learning.ai.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequestDto(
        @NotBlank(message = "Prompt cannot be blank")
        String prompt,
        String sessionId
) {}
