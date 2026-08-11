package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpsertAboutCertificateRequest(
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest issuer,
        @NotNull @Valid LocalizedTextRequest description,
        LocalDate issueDate,
        @Size(max = 4096) String imageUrl,
        @NotNull @Min(0) @Max(10000) Integer displayOrder
) {
}
