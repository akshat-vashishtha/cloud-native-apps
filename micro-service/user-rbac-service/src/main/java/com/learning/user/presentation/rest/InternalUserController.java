package com.learning.user.presentation.rest;

import com.learning.user.application.dto.InternalUserAuthDetailsDto;
import com.learning.user.application.exception.UserNotFoundException;
import com.learning.user.application.service.RoleService;
import com.learning.user.domain.User;
import com.learning.user.infrastructure.config.InternalApiProperties;
import com.learning.user.infrastructure.mapper.UserMapper;
import com.learning.user.infrastructure.persistence.UserDocument;
import com.learning.user.infrastructure.persistence.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalUserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final InternalApiProperties internalApiProperties;

    public InternalUserController(
            UserRepository userRepository,
            UserMapper userMapper,
            RoleService roleService,
            InternalApiProperties internalApiProperties
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.internalApiProperties = internalApiProperties;
    }

    @GetMapping("/users/by-email")
    public ResponseEntity<InternalUserAuthDetailsDto> getByEmail(
            @RequestParam String email,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey
    ) {
        requireInternalApiKey(apiKey);

        UserDocument document = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
        User user = userMapper.toDomain(document);
        Set<String> permissions = roleService.resolvePermissionsForRoles(user.getRoles());

        return ResponseEntity.ok(new InternalUserAuthDetailsDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                new ArrayList<>(user.getRoles()),
                permissions,
                user.getStatus().name()
        ));
    }

    private void requireInternalApiKey(String apiKey) {
        if (apiKey == null || !apiKey.equals(internalApiProperties.apiKey())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal API key");
        }
    }
}
