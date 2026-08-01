package com.example.elhabashyback.auth.dto;

import com.example.elhabashyback.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
