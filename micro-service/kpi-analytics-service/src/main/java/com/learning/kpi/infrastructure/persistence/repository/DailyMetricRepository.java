package com.learning.kpi.infrastructure.persistence.repository;

import com.learning.kpi.infrastructure.persistence.document.DailyMetricDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMetricRepository extends MongoRepository<DailyMetricDocument, String> {

    Optional<DailyMetricDocument> findByCurrencyAndWindowStart(String currency, Instant windowStart);

    List<DailyMetricDocument> findTop30ByCurrencyOrderByWindowStartDesc(String currency);
}
