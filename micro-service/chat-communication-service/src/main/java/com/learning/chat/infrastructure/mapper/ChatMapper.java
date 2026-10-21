package com.learning.chat.infrastructure.mapper;

import com.learning.chat.application.dto.ChatMessageResponseDto;
import com.learning.chat.domain.model.ChatMessage;
import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(target = "version", ignore = true)
    ChatMessageDocument toDocument(ChatMessage domain);

    ChatMessage toDomain(ChatMessageDocument document);

    ChatMessageResponseDto toResponseDto(ChatMessageDocument document);

    ChatMessageResponseDto toResponseDto(ChatMessage domain);
}