package com.learning.user.presentation.rest;

import com.learning.user.application.dto.AuthResponseDto;
import com.learning.user.application.dto.LoginRequestDto;
import com.learning.user.application.dto.PublicKeyDto;
import com.learning.user.application.dto.RegisterRequestDto;
import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.service.AuthenticationService;
import com.learning.user.infrastructure.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authService;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationService authService, JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/public-key")
    public ResponseEntity<PublicKeyDto> getPublicKey() {
        String base64Key = Base64.getEncoder().encodeToString(jwtTokenService.getRsaPublicKey().getEncoded());
        return ResponseEntity.ok(new PublicKeyDto("RS256", base64Key));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String internalUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(authService.getCurrentUser(internalUserId, authHeader));
    }
}
