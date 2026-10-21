package com.learning.user.presentation.rest;

import com.learning.user.application.dto.CreateRoleRequestDto;
import com.learning.user.application.dto.RoleDto;
import com.learning.user.application.dto.UpdateRolePermissionsDto;
import com.learning.user.application.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasAuthority('user:manage_roles')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody CreateRoleRequestDto requestDto) {
        RoleDto role = roleService.createRole(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleName}")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String roleName) {
        return ResponseEntity.ok(roleService.getRoleByName(roleName));
    }

    @PutMapping("/{roleName}/permissions")
    public ResponseEntity<RoleDto> updateRolePermissions(
            @PathVariable String roleName,
            @Valid @RequestBody UpdateRolePermissionsDto requestDto) {
        RoleDto updatedRole = roleService.updateRolePermissions(roleName, requestDto);
        return ResponseEntity.ok(updatedRole);
    }
}
