package com.learning.chat.infrastructure.persistence.repository;

import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    // Fetch the last 50 messages for a room/channel in reverse chronological order
    List<ChatMessageDocument> findTop50ByChannelIdOrderByTimestampDesc(String channelId);
    // Fetch user audit history
    List<ChatMessageDocument> findTop50BySenderIdOrderByTimestampDesc(String senderId);
}
