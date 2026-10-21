package com.learning.user.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private Set<String> permissions;

    private boolean isSystemRole;

    private Instant createdAt;

    private Instant updatedAt;

    @Version
    private Long version;
}
