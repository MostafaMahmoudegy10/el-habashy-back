package com.example.elhabashyback.media.service;

public record CloudinaryUploadResult(
        String secureUrl,
        String publicId,
        String resourceType,
        String format,
        Integer width,
        Integer height,
        long bytes,
        Double duration,
        long version,
        String signature
) {
}
