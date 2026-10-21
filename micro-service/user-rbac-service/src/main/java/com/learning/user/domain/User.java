package com.learning.user.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private Set<String> roles;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static User createNew(String id, String email, String passwordHash, String fullName) {
        return new User(
                id,
                email,
                passwordHash,
                fullName,
                Set.of("ROLE_USER"),
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }

    public User(String id, String email, String passwordHash, String fullName,
                Set<String> roles, UserStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>(Set.of("ROLE_USER"));
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public void assignRole(String role) {
        if (role != null && !role.isBlank()) {
            this.roles.add(role.trim());
            this.updatedAt = Instant.now();
        }
    }

    public void removeRole(String role) {
        if (role != null) {
            this.roles.remove(role.trim());
            this.updatedAt = Instant.now();
        }
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void delete() {
        this.status = UserStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public Set<String> getRoles() { return Collections.unmodifiableSet(roles); }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
