package com.learning.chat.presentation.rest;

import com.learning.chat.application.dto.ChatHistoryResponseDto;
import com.learning.chat.application.dto.ChatMessageResponseDto;
import com.learning.chat.infrastructure.mapper.ChatMapper;
import com.learning.chat.infrastructure.persistence.criteria.ChatCriteriaQueryAdapter;
import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ai:chat') or hasAuthority('chat:support_agent')")
public class ChatHistoryRestController {

    private final ChatCriteriaQueryAdapter criteriaQueryAdapter;
    private final ChatMapper chatMapper;

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<ChatHistoryResponseDto> getChannelHistory(@PathVariable String channelId) {
        log.info("REST: Fetching message history for channel via Criteria: {}", channelId);
        List<ChatMessageDocument> recentDocs = new ArrayList<>(
                criteriaQueryAdapter.findRecentMessagesByChannel(channelId, 50)
        );
        Collections.reverse(recentDocs);

        List<ChatMessageResponseDto> messages = recentDocs.stream()
                .map(chatMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(new ChatHistoryResponseDto(channelId, messages.size(), messages));
    }
}
