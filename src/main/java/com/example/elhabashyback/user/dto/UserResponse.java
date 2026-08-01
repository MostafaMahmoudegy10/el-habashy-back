package com.example.elhabashyback.user.dto;

import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.entity.Users;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt
) {
    public static UserResponse from(Users user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                Boolean.TRUE.equals(user.getEnabled()),
                user.getCreatesAt()
        );
    }
}
