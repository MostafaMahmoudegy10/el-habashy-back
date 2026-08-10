package com.example.elhabashyback.media.service;

import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class CloudinarySignatureService {

    private final CloudinaryProperties properties;

    public String signUpload(String publicId, long timestamp) {
        return sha1("public_id=" + publicId + "&timestamp=" + timestamp + properties.apiSecret());
    }

    public boolean verifyUploadResponse(String publicId, long version, String signature) {
        String expected = sha1("public_id=" + publicId + "&version=" + version + properties.apiSecret());
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                signature.toLowerCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is unavailable", exception);
        }
    }
}
