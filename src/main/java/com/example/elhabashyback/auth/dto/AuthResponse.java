package com.example.elhabashyback.auth.dto;

import com.example.elhabashyback.user.dto.UserResponse;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Instant expiresAt,
        UserResponse user
) {
}
