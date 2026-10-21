package com.learning.banking.infrastructure.persistence.repository;

import com.learning.banking.infrastructure.persistence.document.OutboxEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEventDocument, String> {
    List<OutboxEventDocument> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
