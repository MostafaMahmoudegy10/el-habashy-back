package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.media.service.CloudinaryUploadResult;

public record AboutImageResponse(
        String url,
        String publicId,
        String format,
        Integer width,
        Integer height,
        Long bytes
) {
    public static AboutImageResponse from(CloudinaryUploadResult result) {
        return new AboutImageResponse(
                result.secureUrl(),
                result.publicId(),
                result.format(),
                result.width(),
                result.height(),
                result.bytes()
        );
    }
}
