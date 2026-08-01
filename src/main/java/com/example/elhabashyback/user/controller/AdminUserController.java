package com.example.elhabashyback.user.controller;

import com.example.elhabashyback.common.dto.PageResponse;
import com.example.elhabashyback.user.dto.UpdateUserRoleRequest;
import com.example.elhabashyback.user.dto.UpdateUserStatusRequest;
import com.example.elhabashyback.user.dto.UserResponse;
import com.example.elhabashyback.user.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping
    PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(userAdminService.list(pageable));
    }

    @PatchMapping("/{id}/role")
    UserResponse updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return userAdminService.updateRole(id, request.role());
    }

    @PatchMapping("/{id}/status")
    UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userAdminService.updateStatus(id, request.enabled());
    }
}
