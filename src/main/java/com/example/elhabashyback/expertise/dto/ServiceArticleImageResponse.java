package com.example.elhabashyback.expertise.dto;

import com.example.elhabashyback.media.service.CloudinaryUploadResult;

public record ServiceArticleImageResponse(
        String url,
        String publicId,
        String format,
        Integer width,
        Integer height,
        Long bytes
) {
    public static ServiceArticleImageResponse from(CloudinaryUploadResult result) {
        return new ServiceArticleImageResponse(
                result.secureUrl(),
                result.publicId(),
                result.format(),
                result.width(),
                result.height(),
                result.bytes()
        );
    }
}
