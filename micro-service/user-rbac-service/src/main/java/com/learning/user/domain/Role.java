package com.learning.user.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {

    private final String id;
    private final String name;
    private final String description;
    private final Set<String> permissions;
    private final boolean isSystemRole;

    public Role(String id, String name, String description, Set<String> permissions, boolean isSystemRole) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Role name cannot be null");
        this.description = description;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
        this.isSystemRole = isSystemRole;
    }

    public static Role of(String name, String description, Set<String> permissions) {
        return new Role(null, name, description, permissions, false);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<String> getPermissions() { return Collections.unmodifiableSet(permissions); }
    public boolean isSystemRole() { return isSystemRole; }
}
