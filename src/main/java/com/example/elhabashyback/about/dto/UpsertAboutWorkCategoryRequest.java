package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertAboutWorkCategoryRequest(
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest summary,
        @NotNull @Min(0) @Max(10000) Integer displayOrder
) {
}
