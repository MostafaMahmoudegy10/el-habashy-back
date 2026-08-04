package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.listing.entity.MediaRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMediaUploadRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 120) String contentType,
        @Positive long bytes,
        @NotNull MediaRole role
) {
}
