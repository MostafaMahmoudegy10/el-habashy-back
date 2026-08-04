package com.example.elhabashyback.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailMediaUploadRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
