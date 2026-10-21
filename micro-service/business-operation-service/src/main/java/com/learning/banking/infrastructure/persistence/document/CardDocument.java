package com.learning.banking.infrastructure.persistence.document;

import com.learning.banking.domain.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document( collection = "cards")
@CompoundIndex( name = "account_cards_idx", def = "{'accountId': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDocument {

    @Id
    private String id;
    private String userId;
    private String accountId;
    private String cardNumberMasked;
    private CardStatus status;
    private String expiryDate;
    private Instant createdAt;
}
