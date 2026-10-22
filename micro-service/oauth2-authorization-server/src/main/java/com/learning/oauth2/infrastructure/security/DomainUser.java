package com.learning.oauth2.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DomainUser implements UserDetails {

    private final String userId;
    private final String email;
    private final String fullName;
    private final String passwordHash;
    private final List<String> roles;
    private final Set<String> permissions;
    private final boolean active;

    public DomainUser(
            String userId,
            String email,
            String fullName,
            String passwordHash,
            List<String> roles,
            Set<String> permissions,
            boolean active
    ) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
        this.permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
        this.active = active;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Stream<GrantedAuthority> roleAuthorities = roles.stream().map(SimpleGrantedAuthority::new);
        Stream<GrantedAuthority> permissionAuthorities = permissions.stream().map(SimpleGrantedAuthority::new);
        return Stream.concat(roleAuthorities, permissionAuthorities).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
