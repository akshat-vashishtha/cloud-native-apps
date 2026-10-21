package com.learning.banking.infrastructure.persistence.repository;

import com.learning.banking.infrastructure.persistence.document.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findBySourceAccountIdOrTargetAccountIdOrderByCreatedAtDesc(String sourceAccountId, String targetAccountId);
}
