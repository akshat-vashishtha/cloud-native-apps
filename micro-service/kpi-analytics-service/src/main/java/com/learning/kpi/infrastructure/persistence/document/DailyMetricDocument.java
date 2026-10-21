package com.learning.kpi.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "daily_metrics")
@CompoundIndex(name = "currency_start_idx", def = "{'currency': 1, 'windowStart': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetricDocument {

    @Id
    private String id;
    private String currency;
    private double totalVolume;
    private long transactionCount;
    private Map<String, Long> typeCounts;
    private Instant windowStart;
    private Instant updatedAt;

    @Version
    private Long version;
}
