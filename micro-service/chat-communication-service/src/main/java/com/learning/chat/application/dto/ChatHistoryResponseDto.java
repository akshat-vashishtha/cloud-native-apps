package com.learning.chat.application.dto;

import java.util.List;

public record ChatHistoryResponseDto(
        String channelId,
        int totalMessages,
        List<ChatMessageResponseDto> messages
) {}
