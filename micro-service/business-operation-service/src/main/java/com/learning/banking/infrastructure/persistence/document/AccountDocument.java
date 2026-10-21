package com.learning.banking.infrastructure.persistence.document;

import com.learning.banking.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "accounts")
@CompoundIndexes({
        @CompoundIndex(name = "user_id_idx", def = "{'userId' : 1 }"),
        @CompoundIndex(name = "account_number_unique_idx", def = "{'accountNumber' : 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument {

    @Id
    private String id;
    private String userId;
    private String accountNumber;
    private BigDecimal balanceAmount;
    private String currency;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updateAt;

    @Version
    private Long version;
}
