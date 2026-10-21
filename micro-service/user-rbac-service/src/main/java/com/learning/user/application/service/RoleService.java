package com.learning.user.application.service;

import com.learning.user.application.dto.CreateRoleRequestDto;
import com.learning.user.application.dto.RoleDto;
import com.learning.user.application.dto.UpdateRolePermissionsDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RoleService {

    Set<String> resolvePermissionsForRoles(Collection<String> roleNames);

    RoleDto createRole(CreateRoleRequestDto requestDto);

    RoleDto updateRolePermissions(String roleName, UpdateRolePermissionsDto requestDto);

    List<RoleDto> getAllRoles();

    RoleDto getRoleByName(String roleName);
}
