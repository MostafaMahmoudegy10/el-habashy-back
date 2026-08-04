package com.example.elhabashyback.configuration.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(
        String cloudName,
        String apiKey,
        String apiSecret,
        String folder,
        String apiBaseUrl
) {
    public boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    public String resolvedApiBaseUrl() {
        String value = apiBaseUrl == null || apiBaseUrl.isBlank()
                ? "https://api.cloudinary.com"
                : apiBaseUrl.trim();
        return value.replaceAll("/+$", "");
    }
}
