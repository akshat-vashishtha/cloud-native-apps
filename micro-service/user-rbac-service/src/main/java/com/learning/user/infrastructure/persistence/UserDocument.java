package com.learning.user.infrastructure.persistence;

import com.learning.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "users")
@CompoundIndexes({
        @CompoundIndex(name = "email_unique_idx", def = "{'email': 1}", unique = true),
        @CompoundIndex(name = "roles_idx", def = "{'roles': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;
    private String email;
    private String passwordHash;
    private String fullName;
    private Set<String> roles;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private Long version;
}
