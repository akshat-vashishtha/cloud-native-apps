package com.learning.banking.infrastructure.persistence.repository;

import com.learning.banking.infrastructure.persistence.document.CardDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends MongoRepository<CardDocument, String> {
    List<CardDocument> findByAccountId(String accountId);
    Optional<CardDocument> findByIdAndUserId(String id, String userId);
}
