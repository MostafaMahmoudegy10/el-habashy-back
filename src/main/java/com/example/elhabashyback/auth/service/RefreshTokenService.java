package com.example.elhabashyback.auth.service;

import com.example.elhabashyback.auth.entity.RefreshToken;
import com.example.elhabashyback.auth.repository.RefreshTokenRepository;
import com.example.elhabashyback.common.exception.UnauthorizedException;
import com.example.elhabashyback.configuration.security.AppJwtProperties;
import com.example.elhabashyback.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppJwtProperties properties;

    @Transactional
    public IssuedRefreshToken issue(Users user) {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(properties.refreshTokenTtl()));
        refreshTokenRepository.save(token);

        return new IssuedRefreshToken(rawToken, user);
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        Instant now = Instant.now();
        if (!storedToken.isActive(now) || !Boolean.TRUE.equals(storedToken.getUser().getEnabled())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        storedToken.setRevokedAt(now);
        return issue(storedToken.getUser());
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
            }
        });
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public record IssuedRefreshToken(String value, Users user) {
    }
}
