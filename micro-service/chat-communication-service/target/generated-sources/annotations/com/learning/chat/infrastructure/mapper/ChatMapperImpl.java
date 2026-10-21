package com.learning.chat.infrastructure.mapper;

import com.learning.chat.application.dto.ChatMessageResponseDto;
import com.learning.chat.domain.enums.MessageType;
import com.learning.chat.domain.model.ChatMessage;
import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-21T15:41:22+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class ChatMapperImpl implements ChatMapper {

    @Override
    public ChatMessageDocument toDocument(ChatMessage domain) {
        if ( domain == null ) {
            return null;
        }

        ChatMessageDocument.ChatMessageDocumentBuilder chatMessageDocument = ChatMessageDocument.builder();

        chatMessageDocument.id( domain.getId() );
        chatMessageDocument.channelId( domain.getChannelId() );
        chatMessageDocument.senderId( domain.getSenderId() );
        chatMessageDocument.recipientId( domain.getRecipientId() );
        chatMessageDocument.content( domain.getContent() );
        chatMessageDocument.messageType( domain.getMessageType() );
        chatMessageDocument.timestamp( domain.getTimestamp() );

        return chatMessageDocument.build();
    }

    @Override
    public ChatMessage toDomain(ChatMessageDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String channelId = null;
        String senderId = null;
        String recipientId = null;
        String content = null;
        MessageType messageType = null;
        Instant timestamp = null;

        id = document.getId();
        channelId = document.getChannelId();
        senderId = document.getSenderId();
        recipientId = document.getRecipientId();
        content = document.getContent();
        messageType = document.getMessageType();
        timestamp = document.getTimestamp();

        ChatMessage chatMessage = new ChatMessage( id, channelId, senderId, recipientId, content, messageType, timestamp );

        return chatMessage;
    }

    @Override
    public ChatMessageResponseDto toResponseDto(ChatMessageDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String channelId = null;
        String senderId = null;
        String recipientId = null;
        String content = null;
        MessageType messageType = null;
        Instant timestamp = null;

        id = document.getId();
        channelId = document.getChannelId();
        senderId = document.getSenderId();
        recipientId = document.getRecipientId();
        content = document.getContent();
        messageType = document.getMessageType();
        timestamp = document.getTimestamp();

        ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto( id, channelId, senderId, recipientId, content, messageType, timestamp );

        return chatMessageResponseDto;
    }

    @Override
    public ChatMessageResponseDto toResponseDto(ChatMessage domain) {
        if ( domain == null ) {
            return null;
        }

        String id = null;
        String channelId = null;
        String senderId = null;
        String recipientId = null;
        String content = null;
        MessageType messageType = null;
        Instant timestamp = null;

        id = domain.getId();
        channelId = domain.getChannelId();
        senderId = domain.getSenderId();
        recipientId = domain.getRecipientId();
        content = domain.getContent();
        messageType = domain.getMessageType();
        timestamp = domain.getTimestamp();

        ChatMessageResponseDto chatMessageResponseDto = new ChatMessageResponseDto( id, channelId, senderId, recipientId, content, messageType, timestamp );

        return chatMessageResponseDto;
    }
}
