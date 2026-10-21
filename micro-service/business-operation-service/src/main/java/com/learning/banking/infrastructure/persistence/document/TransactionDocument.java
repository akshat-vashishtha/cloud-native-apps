package com.learning.banking.infrastructure.persistence.document;

import com.learning.banking.domain.enums.TransactionStatus;
import com.learning.banking.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "transactions")
@CompoundIndexes({
        @CompoundIndex(name = "src_acc_created_idx", def = "{'sourceAccountId' : 1, 'createdAt' : -1 }"),
        @CompoundIndex(name = "tgt_acc_created_idx", def = "{'targetAccountId' : 1, 'createdAt' : -1 }")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDocument {

    @Id
    private String id;
    private String sourceAccountId;
    private String targetAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private Instant createdAt;
}
