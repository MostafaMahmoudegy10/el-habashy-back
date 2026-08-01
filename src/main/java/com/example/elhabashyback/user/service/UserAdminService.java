package com.example.elhabashyback.user.service;

import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.user.dto.UserResponse;
import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional
    public UserResponse updateRole(UUID id, Role role) {
        Users user = getUser(id);
        user.setRole(role);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateStatus(UUID id, boolean enabled) {
        Users user = getUser(id);
        user.setEnabled(enabled);
        return UserResponse.from(user);
    }

    private Users getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
