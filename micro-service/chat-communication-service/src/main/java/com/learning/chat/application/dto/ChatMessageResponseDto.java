package com.learning.chat.application.dto;

import com.learning.chat.domain.enums.MessageType;
import java.time.Instant;

public record ChatMessageResponseDto(
        String id,
        String channelId,
        String senderId,
        String recipientId,
        String content,
        MessageType messageType,
        Instant timestamp
) {}
