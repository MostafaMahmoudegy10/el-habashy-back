package com.example.elhabashyback.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CompleteMediaUploadRequest(
        @NotBlank @Size(max = 4096) String secureUrl,
        @NotBlank @Size(max = 300) String publicId,
        @NotBlank @Size(max = 20) String resourceType,
        @NotBlank @Size(max = 50) String format,
        @PositiveOrZero Integer width,
        @PositiveOrZero Integer height,
        @Positive long bytes,
        @PositiveOrZero Double duration,
        @Positive long version,
        @NotBlank @Size(max = 128) String signature
) {
}
