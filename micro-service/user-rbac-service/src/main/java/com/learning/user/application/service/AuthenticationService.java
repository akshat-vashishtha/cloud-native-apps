package com.learning.user.application.service;

import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.RegisterResponseDto;
import com.learning.user.application.dto.UserProfileDto;

public interface AuthenticationService {
    RegisterResponseDto register(RegisterRequestDto registerRequestDto);

    UserProfileDto getCurrentUserProfile();
}
