package com.learning.user.presentation.rest;

import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.RegisterResponseDto;
import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }
}
