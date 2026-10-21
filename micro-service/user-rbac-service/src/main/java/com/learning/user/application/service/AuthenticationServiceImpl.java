package com.learning.user.application.service;

import com.learning.user.application.dto.AuthResponseDto;
import com.learning.user.application.dto.LoginRequestDto;
import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.exception.EmailAlreadyExistsException;
import com.learning.user.application.exception.InvalidCredentialsException;
import com.learning.user.domain.User;
import com.learning.user.domain.UserStatus;
import com.learning.user.infrastructure.mapper.UserMapper;
import com.learning.user.infrastructure.persistence.UserDocument;
import com.learning.user.infrastructure.persistence.UserRepository;
import com.learning.user.infrastructure.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final UserService userService;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtTokenService jwtTokenService,
                                   UserMapper userMapper,
                                   UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {

        String email = registerRequestDto.email();
        String password = registerRequestDto.password();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists " + email);
        }

        String hashPassword = passwordEncoder.encode(password);
        String userId = UUID.randomUUID().toString();

        User user = User.createNew(userId, email, hashPassword, registerRequestDto.fullName());

        UserDocument userDocument = userMapper.toDocument(user);
        UserDocument savedDocument = userRepository.save(userDocument);

        String accessToken = jwtTokenService.generateToken(user);

         return AuthResponseDto.bearer(savedDocument.getId(), savedDocument.getEmail(), accessToken);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.email();
        String password = loginRequestDto.password();

        UserDocument userDocument = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        if (!passwordEncoder.matches(password, userDocument.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        if (userDocument.getStatus().equals(UserStatus.SUSPENDED)) {
            throw new InvalidCredentialsException("User account is suspended");
        }

        User user = userMapper.toDomain(userDocument);
        String accessToken = jwtTokenService.generateToken(user);

        return AuthResponseDto.bearer(userDocument.getId(), userDocument.getEmail(), accessToken);
    }

    @Override
    public UserProfileDto getCurrentUser(String internalUserId, String authHeader) {
        String userId = internalUserId;

        // Fallback: If caller sent Bearer token directly
        if ((userId == null || userId.isBlank()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                userId = jwtTokenService.validateAndExtractUserId(token);
            } catch (Exception e) {
                throw new InvalidCredentialsException("Invalid or expired Bearer token");
            }
        }

        if (userId == null || userId.isBlank()) {
            throw new InvalidCredentialsException("Missing or unauthorized identity headers");
        }

        return userService.getUserProfileById(userId);
    }
}
