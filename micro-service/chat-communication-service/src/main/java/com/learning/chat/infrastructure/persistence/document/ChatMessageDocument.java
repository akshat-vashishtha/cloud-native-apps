package com.learning.chat.infrastructure.persistence.document;

import com.learning.chat.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@CompoundIndexes({
        // Channel conversation lookup (most recent first)
        @CompoundIndex(name = "channel_time_idx", def = "{'channelId': 1, 'timestamp': -1}"),
        // User audit lookup
        @CompoundIndex(name = "sender_time_idx", def = "{'senderId': 1, 'timestamp': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDocument {

    @Id
    private String id;
    private String channelId;
    private String senderId;
    private String recipientId;
    private String content;
    private MessageType messageType;
    private Instant timestamp;

    @Version
    private Long version;
}
