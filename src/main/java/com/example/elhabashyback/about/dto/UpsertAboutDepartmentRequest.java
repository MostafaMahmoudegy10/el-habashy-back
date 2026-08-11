package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertAboutDepartmentRequest(
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest description,
        @NotNull @Min(0) @Max(10000) Integer displayOrder
) {
}
