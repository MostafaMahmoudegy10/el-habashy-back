package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAboutProfileRequest(
        @NotNull @Valid LocalizedTextRequest headline,
        @NotNull @Valid LocalizedTextRequest profile,
        @NotNull @Valid LocalizedTextRequest mission,
        @NotNull @Valid LocalizedTextRequest vision,
        @Size(max = 4096) String imageUrl,
        @NotNull @Min(1900) @Max(2100) Integer startedYear
) {
}
