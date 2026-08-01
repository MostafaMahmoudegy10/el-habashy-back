package com.example.elhabashyback.user.dto;

import com.example.elhabashyback.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {
}
