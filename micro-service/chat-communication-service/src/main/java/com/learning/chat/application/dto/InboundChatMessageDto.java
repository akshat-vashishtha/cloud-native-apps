package com.learning.chat.application.dto;

import com.learning.chat.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InboundChatMessageDto(
        @NotBlank(message = "Content must not be blank")
        String content,

        @NotNull(message = "MessageType must not be null")
        MessageType messageType
) {}
