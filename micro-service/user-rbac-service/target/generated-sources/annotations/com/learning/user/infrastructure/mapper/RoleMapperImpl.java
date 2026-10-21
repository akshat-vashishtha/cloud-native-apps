package com.learning.user.infrastructure.mapper;

import com.learning.user.application.dto.RoleDto;
import com.learning.user.domain.Role;
import com.learning.user.infrastructure.persistence.RoleDocument;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T14:25:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleDocument toDocument(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleDocument.RoleDocumentBuilder roleDocument = RoleDocument.builder();

        roleDocument.id( role.getId() );
        roleDocument.name( role.getName() );
        roleDocument.description( role.getDescription() );
        Set<String> set = role.getPermissions();
        if ( set != null ) {
            roleDocument.permissions( new LinkedHashSet<String>( set ) );
        }

        return roleDocument.build();
    }

    @Override
    public Role toDomain(RoleDocument document) {
        if ( document == null ) {
            return null;
        }

        Set<String> permissions = null;
        String id = null;
        String name = null;
        String description = null;

        Set<String> set = document.getPermissions();
        if ( set != null ) {
            permissions = new LinkedHashSet<String>( set );
        }
        id = document.getId();
        name = document.getName();
        description = document.getDescription();

        boolean isSystemRole = false;

        Role role = new Role( id, name, description, permissions, isSystemRole );

        return role;
    }

    @Override
    public RoleDto toDto(RoleDocument document) {
        if ( document == null ) {
            return null;
        }

        String id = null;
        String name = null;
        String description = null;
        Set<String> permissions = null;

        id = document.getId();
        name = document.getName();
        description = document.getDescription();
        Set<String> set = document.getPermissions();
        if ( set != null ) {
            permissions = new LinkedHashSet<String>( set );
        }

        boolean isSystemRole = false;

        RoleDto roleDto = new RoleDto( id, name, description, permissions, isSystemRole );

        return roleDto;
    }

    @Override
    public RoleDto toDto(Role role) {
        if ( role == null ) {
            return null;
        }

        String id = null;
        String name = null;
        String description = null;
        Set<String> permissions = null;

        id = role.getId();
        name = role.getName();
        description = role.getDescription();
        Set<String> set = role.getPermissions();
        if ( set != null ) {
            permissions = new LinkedHashSet<String>( set );
        }

        boolean isSystemRole = false;

        RoleDto roleDto = new RoleDto( id, name, description, permissions, isSystemRole );

        return roleDto;
    }
}
