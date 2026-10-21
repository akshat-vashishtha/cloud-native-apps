package com.learning.chat.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectMessageDto(
        @NotBlank(message = "Recipient ID must not be blank")
        String recipientId,

        @NotBlank(message = "Content must not be blank")
        String content
) {}
