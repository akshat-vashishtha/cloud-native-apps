package com.learning.chat.domain.model;

import com.learning.chat.domain.enums.MessageType;
import java.time.Instant;
import java.util.Objects;

public class ChatMessage {

    private final String id;
    private final String channelId;
    private final String senderId;
    private final String recipientId; // Null for group broadcasts
    private final String content;
    private final MessageType messageType;
    private final Instant timestamp;

    public ChatMessage(String id, String channelId, String senderId, String recipientId,
                       String content, MessageType messageType, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "Message ID cannot be null");
        this.channelId = Objects.requireNonNull(channelId, "Channel ID cannot be null");
        this.senderId = Objects.requireNonNull(senderId, "Sender ID cannot be null");
        this.recipientId = recipientId;
        this.content = Objects.requireNonNull(content, "Message content cannot be null");
        this.messageType = Objects.requireNonNull(messageType, "Message type cannot be null");
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static ChatMessage createGroupMessage(String id, String channelId, String senderId, String content) {
        return new ChatMessage(id, channelId, senderId, null, content, MessageType.GROUP_MESSAGE, Instant.now());
    }

    public static ChatMessage createDirectMessage(String id, String channelId, String senderId, String recipientId, String content) {
        return new ChatMessage(id, channelId, senderId, recipientId, content, MessageType.DIRECT_MESSAGE, Instant.now());
    }

    public String getId() { return id; }
    public String getChannelId() { return channelId; }
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public String getContent() { return content; }
    public MessageType getMessageType() { return messageType; }
    public Instant getTimestamp() { return timestamp; }
}
