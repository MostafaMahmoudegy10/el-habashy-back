package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertAboutWorkEntryRequest(
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest client,
        @NotNull @Valid LocalizedTextRequest summary,
        @NotNull @Valid LocalizedTextRequest details,
        @Min(1900) @Max(2100) Integer projectYear,
        @NotNull @Valid LocalizedTextRequest location,
        @Size(max = 4096) String imageUrl,
        @NotNull @Min(0) @Max(10000) Integer displayOrder
) {
}
