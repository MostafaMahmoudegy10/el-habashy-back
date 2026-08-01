package com.example.elhabashyback.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class SecureTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKey jwtSecretKey;

    public String randomToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String numericOtp() {
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    public String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public String hashOtp(String email, String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(jwtSecretKey);
            return HexFormat.of().formatHex(mac.doFinal((email + ":" + otp).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash OTP", exception);
        }
    }

    public boolean matches(String expectedHash, String actualHash) {
        return MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.US_ASCII),
                actualHash.getBytes(StandardCharsets.US_ASCII));
    }
}
