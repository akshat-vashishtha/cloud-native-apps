package com.learning.ai.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_turns")
@CompoundIndex(name = "user_session_ts_idx", def = "{'userId': 1, 'sessionId': 1, 'timestamp': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTurnDocument {

    @Id
    private String id;
    private String userId;
    private String sessionId;
    private String role;
    private String content;
    private Instant timestamp;
}
