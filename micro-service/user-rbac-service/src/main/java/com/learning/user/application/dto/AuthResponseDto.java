package com.learning.user.application.dto;

public record AuthResponseDto(String userId, String email, String accessToken) {
    public static AuthResponseDto bearer(String userId, String email, String accessToken) {
        return new AuthResponseDto(userId, email, accessToken);
    }

}
