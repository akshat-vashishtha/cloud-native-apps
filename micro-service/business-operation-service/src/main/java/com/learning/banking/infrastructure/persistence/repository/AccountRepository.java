package com.learning.banking.infrastructure.persistence.repository;

import com.learning.banking.infrastructure.persistence.document.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<AccountDocument, String> {

    Optional<AccountDocument> findByAccountNumber(String accountNumber);
    List<AccountDocument> findByUserId(String userId);
    boolean existsByAccountNumber(String accountNumber);
}
