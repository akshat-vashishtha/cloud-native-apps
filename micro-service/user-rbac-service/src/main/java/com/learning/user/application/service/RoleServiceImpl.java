package com.learning.user.application.service;

import com.learning.user.application.dto.CreateRoleRequestDto;
import com.learning.user.application.dto.RoleDto;
import com.learning.user.application.dto.UpdateRolePermissionsDto;
import com.learning.user.application.exception.RoleAlreadyExistsException;
import com.learning.user.application.exception.RoleNotFoundException;
import com.learning.user.infrastructure.mapper.RoleMapper;
import com.learning.user.infrastructure.persistence.RoleDocument;
import com.learning.user.infrastructure.persistence.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Set<String> resolvePermissionsForRoles(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        List<RoleDocument> roles = roleRepository.findByNameIn(roleNames);
        Set<String> permissions = new HashSet<>();
        for (RoleDocument role : roles) {
            if (role.getPermissions() != null) {
                permissions.addAll(role.getPermissions());
            }
        }
        return permissions;
    }

    @Override
    public RoleDto createRole(CreateRoleRequestDto requestDto) {
        String roleName = requestDto.name().trim().toUpperCase();
        if (roleRepository.existsByName(roleName)) {
            throw new RoleAlreadyExistsException("Role already exists with name: " + roleName);
        }

        Instant now = Instant.now();
        RoleDocument document = RoleDocument.builder()
                .id(UUID.randomUUID().toString())
                .name(roleName)
                .description(requestDto.description())
                .permissions(new HashSet<>(requestDto.permissions()))
                .isSystemRole(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        RoleDocument saved = roleRepository.save(document);
        log.info("Created dynamic role: {}", roleName);
        return roleMapper.toDto(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "roles", key = "#roleName.toUpperCase()"),
            @CacheEvict(value = "users", allEntries = true)
    })
    public RoleDto updateRolePermissions(String roleName, UpdateRolePermissionsDto requestDto) {
        String normalizedRole = roleName.trim().toUpperCase();
        RoleDocument document = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + normalizedRole));

        document.setPermissions(new HashSet<>(requestDto.permissions()));
        document.setUpdatedAt(Instant.now());

        RoleDocument updated = roleRepository.save(document);
        log.info("Updated permissions for role [{}] -> {}", normalizedRole, requestDto.permissions());
        return roleMapper.toDto(updated);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    @Cacheable(value = "roles", key = "#roleName.toUpperCase()")
    public RoleDto getRoleByName(String roleName) {
        String normalizedRole = roleName.trim().toUpperCase();
        return roleRepository.findByName(normalizedRole)
                .map(roleMapper::toDto)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + normalizedRole));
    }
}
