package com.learning.ai.application.service;

import com.learning.ai.application.dto.AiChatRequestDto;
import com.learning.ai.application.dto.AiChatResponseDto;
import com.learning.ai.application.dto.ChatTurnResponseDto;
import com.learning.ai.application.dto.ConfirmActionResponseDto;

import java.util.List;

public interface AiConciergeService {

    AiChatResponseDto converse(String userId, AiChatRequestDto request);

    ConfirmActionResponseDto confirmPendingAction(String userId, String confirmationToken);

    List<ChatTurnResponseDto> getSessionMemory(String userId, String sessionId);
}
