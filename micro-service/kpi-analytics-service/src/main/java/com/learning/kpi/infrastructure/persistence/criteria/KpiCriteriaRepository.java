package com.learning.kpi.infrastructure.persistence.criteria;

import com.learning.kpi.infrastructure.persistence.document.DailyMetricDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Repository
public class KpiCriteriaRepository {

    private final MongoTemplate mongoTemplate;

    public KpiCriteriaRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void recordDailyTransactionMetric(String currency, double amount, String type) {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Query query = Query.query(Criteria.where("currency").is(currency).and("windowStart").is(todayStart));
        Update update = new Update()
                .inc("totalVolume", amount)
                .inc("transactionCount", 1L)
                .inc("typeCounts." + type, 1L)
                .set("updatedAt", Instant.now())
                .setOnInsert("currency", currency)
                .setOnInsert("windowStart", todayStart);

        mongoTemplate.upsert(query, update, DailyMetricDocument.class);
    }

    public void recordDailyChatEngagementMetric(String messageType) {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        String bucket = "CHAT_ENGAGEMENT";
        Query query = Query.query(Criteria.where("currency").is(bucket).and("windowStart").is(todayStart));
        Update update = new Update()
                .inc("transactionCount", 1L)
                .inc("typeCounts." + messageType, 1L)
                .set("updatedAt", Instant.now())
                .setOnInsert("currency", bucket)
                .setOnInsert("windowStart", todayStart);

        mongoTemplate.upsert(query, update, DailyMetricDocument.class);
    }

    public List<DailyMetricDocument> findRecentMetricsByCurrency(String currency, int limit) {
        Query query = Query.query(Criteria.where("currency").is(currency))
                .with(Sort.by(Sort.Direction.DESC, "windowStart"))
                .limit(limit);
        return mongoTemplate.find(query, DailyMetricDocument.class);
    }
}
