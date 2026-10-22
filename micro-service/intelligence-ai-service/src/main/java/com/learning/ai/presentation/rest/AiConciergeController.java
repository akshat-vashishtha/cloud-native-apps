package com.learning.ai.presentation.rest;

import com.learning.ai.application.dto.AiChatRequestDto;
import com.learning.ai.application.dto.AiChatResponseDto;
import com.learning.ai.application.dto.ConfirmActionRequestDto;
import com.learning.ai.application.dto.ConfirmActionResponseDto;
import com.learning.ai.application.dto.ChatTurnResponseDto;
import com.learning.ai.application.service.AiConciergeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@PreAuthorize("hasAuthority('ai:chat')")
public class AiConciergeController {

    private final AiConciergeService aiConciergeService;

    public AiConciergeController(AiConciergeService aiConciergeService) {
        this.aiConciergeService = aiConciergeService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AiChatRequestDto request
    ) {
        return ResponseEntity.ok(aiConciergeService.converse(jwt.getSubject(), request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmActionResponseDto> confirmAction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConfirmActionRequestDto request
    ) {
        return ResponseEntity.ok(
                aiConciergeService.confirmPendingAction(jwt.getSubject(), request.confirmationToken())
        );
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatTurnResponseDto>> getSessionHistory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(aiConciergeService.getSessionMemory(jwt.getSubject(), sessionId));
    }
}
