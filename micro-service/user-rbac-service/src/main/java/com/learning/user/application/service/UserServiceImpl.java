package com.learning.user.application.service;

import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.exception.UserNotFoundException;
import com.learning.user.domain.User;
import com.learning.user.infrastructure.mapper.UserMapper;
import com.learning.user.infrastructure.persistence.UserDocument;
import com.learning.user.infrastructure.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, RoleService roleService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleService = roleService;
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserProfileDto getUserProfileById(String userId) {
        log.info("Fetching user profile [{}] from MongoDB (cache miss)", userId);
        UserDocument userDocument = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        User user = userMapper.toDomain(userDocument);
        Set<String> permissions = roleService.resolvePermissionsForRoles(user.getRoles());

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                new ArrayList<>(user.getRoles()),
                permissions,
                user.getStatus().name()
        );
    }

    @Override
    public boolean validateUserPermission(String userId, String requiredPermission) {
        UserProfileDto profile = getUserProfileById(userId);
        return profile.permissions().contains(requiredPermission);
    }
}
