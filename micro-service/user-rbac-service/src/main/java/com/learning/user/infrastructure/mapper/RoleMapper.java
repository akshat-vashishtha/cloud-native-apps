package com.learning.user.infrastructure.mapper;

import com.learning.user.application.dto.RoleDto;
import com.learning.user.domain.Role;
import com.learning.user.infrastructure.persistence.RoleDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "version", ignore = true)
    RoleDocument toDocument(Role role);

    Role toDomain(RoleDocument document);

    RoleDto toDto(RoleDocument document);

    RoleDto toDto(Role role);
}
