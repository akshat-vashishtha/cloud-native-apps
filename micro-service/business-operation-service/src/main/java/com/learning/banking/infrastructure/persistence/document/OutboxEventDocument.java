package com.learning.banking.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document( collection = "outbox_events")
@CompoundIndex(name = "outbox_polling_idx", def = "{'status' : 1, 'createdAt' : 1 }")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventDocument {

    @Id
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private String status;
    private Instant createdAt;

    @Builder.Default
    private int retryCount = 0;

    @Indexed(name = "outbox_ttl_idx", expireAfter = "7d")
    private Instant processedAt;

    @Version
    private Long version;
}
