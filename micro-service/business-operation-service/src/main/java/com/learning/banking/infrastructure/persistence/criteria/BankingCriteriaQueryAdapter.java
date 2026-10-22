package com.learning.banking.infrastructure.persistence.criteria;

import com.learning.banking.infrastructure.persistence.document.AccountDocument;
import com.learning.banking.infrastructure.persistence.document.CardDocument;
import com.learning.banking.infrastructure.persistence.document.OutboxEventDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class BankingCriteriaQueryAdapter {

    private final MongoTemplate mongoTemplate;

    public BankingCriteriaQueryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<AccountDocument> findAccountByNumber(String accountNumber) {
        Query query = Query.query(Criteria.where("accountNumber").is(accountNumber));
        return Optional.ofNullable(mongoTemplate.findOne(query, AccountDocument.class));
    }

    public Optional<CardDocument> findCardByIdAndUserId(String cardId, String userId) {
        Criteria criteria = Criteria.where("_id").is(cardId);
        if (userId != null && !userId.isBlank()) {
            criteria = criteria.and("userId").is(userId);
        }
        Query query = Query.query(criteria);
        return Optional.ofNullable(mongoTemplate.findOne(query, CardDocument.class));
    }

    /**
     * Atomically claims the oldest PENDING outbox event by transitioning it to IN_FLIGHT.
     * Prevents duplicate Kafka publishing across horizontally scaled instances.
     */
    public Optional<OutboxEventDocument> claimNextPendingOutboxEvent() {
        Query query = Query.query(Criteria.where("status").is("PENDING"))
                .with(Sort.by(Sort.Direction.ASC, "createdAt"));

        Update update = new Update()
                .set("status", "IN_FLIGHT")
                .inc("version", 1L);

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, OutboxEventDocument.class));
    }

    public void markOutboxPublished(String eventId) {
        Query query = Query.query(Criteria.where("_id").is(eventId));
        Update update = new Update()
                .set("status", "PUBLISHED")
                .set("processedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, OutboxEventDocument.class);
    }

    public void markOutboxRetryOrFailed(String eventId, int nextRetryCount, int maxRetries) {
        String nextStatus = nextRetryCount >= maxRetries ? "FAILED" : "PENDING";
        Query query = Query.query(Criteria.where("_id").is(eventId));
        Update update = new Update()
                .set("status", nextStatus)
                .set("retryCount", nextRetryCount);
        mongoTemplate.updateFirst(query, update, OutboxEventDocument.class);
    }
}
