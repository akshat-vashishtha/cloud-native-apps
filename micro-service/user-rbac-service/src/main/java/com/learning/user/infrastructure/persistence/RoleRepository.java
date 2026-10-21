package com.learning.user.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<RoleDocument, String> {

    Optional<RoleDocument> findByName(String name);

    List<RoleDocument> findByNameIn(Collection<String> names);

    boolean existsByName(String name);
}
