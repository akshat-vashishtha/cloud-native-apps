package com.learning.chat.infrastructure.persistence.criteria;

import com.learning.chat.infrastructure.persistence.document.ChatMessageDocument;
import com.learning.chat.infrastructure.persistence.document.ChatOutboxEventDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatCriteriaQueryAdapter {

    private final MongoTemplate mongoTemplate;

    public ChatCriteriaQueryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ChatMessageDocument> findRecentMessagesByChannel(String channelId, int limit) {
        Query query = Query.query(Criteria.where("channelId").is(channelId))
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(limit);
        return mongoTemplate.find(query, ChatMessageDocument.class);
    }

    public void saveOutboxEvent(ChatOutboxEventDocument event) {
        mongoTemplate.save(event);
    }

    public Optional<ChatOutboxEventDocument> claimNextPendingOutboxEvent() {
        Query query = Query.query(Criteria.where("status").is("PENDING"))
                .with(Sort.by(Sort.Direction.ASC, "createdAt"));
        Update update = new Update()
                .set("status", "IN_FLIGHT")
                .inc("version", 1L);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, ChatOutboxEventDocument.class));
    }

    public void markOutboxPublished(String eventId) {
        Query query = Query.query(Criteria.where("_id").is(eventId));
        Update update = new Update()
                .set("status", "PUBLISHED")
                .set("processedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, ChatOutboxEventDocument.class);
    }

    public void markOutboxRetryOrFailed(String eventId, int nextRetryCount, int maxRetries) {
        String status = nextRetryCount >= maxRetries ? "FAILED" : "PENDING";
        Query query = Query.query(Criteria.where("_id").is(eventId));
        Update update = new Update()
                .set("status", status)
                .set("retryCount", nextRetryCount);
        mongoTemplate.updateFirst(query, update, ChatOutboxEventDocument.class);
    }
}
