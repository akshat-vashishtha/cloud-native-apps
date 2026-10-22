package com.learning.user.application.service;

import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.RegisterResponseDto;
import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.exception.EmailAlreadyExistsException;
import com.learning.user.application.exception.InvalidCredentialsException;
import com.learning.user.domain.User;
import com.learning.user.infrastructure.mapper.UserMapper;
import com.learning.user.infrastructure.persistence.UserDocument;
import com.learning.user.infrastructure.persistence.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        String email = registerRequestDto.email();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists " + email);
        }

        String hashPassword = passwordEncoder.encode(registerRequestDto.password());
        String userId = UUID.randomUUID().toString();
        User user = User.createNew(userId, email, hashPassword, registerRequestDto.fullName());

        UserDocument savedDocument = userRepository.save(userMapper.toDocument(user));
        return new RegisterResponseDto(savedDocument.getId(), savedDocument.getEmail());
    }

    @Override
    public UserProfileDto getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new InvalidCredentialsException("Missing or unauthorized identity");
        }
        String userId = jwt.getSubject();
        if (userId == null || userId.isBlank()) {
            throw new InvalidCredentialsException("Missing subject in access token");
        }
        return userService.getUserProfileById(userId);
    }
}
