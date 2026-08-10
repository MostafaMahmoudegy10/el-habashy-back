package com.example.elhabashyback.auth.service;

import com.example.elhabashyback.auth.dto.AuthResponse;
import com.example.elhabashyback.auth.dto.LoginRequest;
import com.example.elhabashyback.auth.dto.RegisterRequest;
import com.example.elhabashyback.auth.dto.RegistrationResponse;
import com.example.elhabashyback.common.exception.ConflictException;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.user.dto.UserResponse;
import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import com.example.elhabashyback.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AccountVerificationService accountVerificationService;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        Users user = new Users();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setEnabled(false);
        userRepository.save(user);
        accountVerificationService.sendForNewUser(user);

        return new RegistrationResponse(
                "Account created. Check your email to activate it.",
                user.getEmail()
        );
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizeEmail(request.email()), request.password()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return createSession(principal.getUser());
    }

    @Transactional
    public AuthSession refresh(String rawRefreshToken) {
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.rotate(rawRefreshToken);
        return createResponse(refreshToken.user(), refreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String subject) {
        UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AuthSession createSession(Users user) {
        return createResponse(user, refreshTokenService.issue(user));
    }

    private AuthSession createResponse(Users user, RefreshTokenService.IssuedRefreshToken refreshToken) {
        JwtTokenService.AccessToken accessToken = jwtTokenService.createAccessToken(user);
        long expiresIn = Math.max(0, Duration.between(java.time.Instant.now(), accessToken.expiresAt()).toSeconds());
        AuthResponse response = new AuthResponse(
                accessToken.value(),
                "Bearer",
                expiresIn,
                accessToken.expiresAt(),
                UserResponse.from(user)
        );
        return new AuthSession(response, refreshToken.value());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthSession(AuthResponse response, String refreshToken) {
    }
}
