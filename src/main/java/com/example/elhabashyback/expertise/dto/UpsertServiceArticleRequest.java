package com.example.elhabashyback.expertise.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpsertServiceArticleRequest(
        @NotNull ServiceKind kind,
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest summary,
        @NotNull @Valid LocalizedTextRequest content,
        @NotBlank @Size(max = 4096) String image,
        @NotNull @Size(max = 20) List<@NotBlank @Size(max = 4096) String> gallery,
        @NotNull Boolean featured,
        @NotNull @Min(0) Integer displayOrder,
        @Valid LocalizedTextRequest seoTitle,
        @Valid LocalizedTextRequest seoDescription,
        @Valid LocalizedTextRequest seoKeywords
) {
}
