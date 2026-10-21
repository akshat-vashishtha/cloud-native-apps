package com.learning.user.application.service;

import com.learning.user.application.dto.UserProfileDto;

public interface UserService {
    UserProfileDto getUserProfileById(String userId);
    boolean validateUserPermission(String userId, String requiredPermission);
}
