package com.learning.user.application.service;

import com.learning.user.application.dto.AuthResponseDto;
import com.learning.user.application.dto.LoginRequestDto;
import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.UserProfileDto;

public interface AuthenticationService {
    AuthResponseDto register(RegisterRequestDto registerRequestDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    UserProfileDto getCurrentUser(String internalUserId, String authHeader);
}
