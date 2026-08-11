package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutWorkCategory;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;
import java.util.List;

public record AboutWorkCategoryResponse(
        Long id,
        LocalizedTextResponse title,
        LocalizedTextResponse summary,
        Integer displayOrder,
        List<AboutWorkEntryResponse> entries,
        Instant updatedAt
) {
    public static AboutWorkCategoryResponse from(
            AboutWorkCategory category,
            List<AboutWorkEntryResponse> entries
    ) {
        return new AboutWorkCategoryResponse(
                category.getId(),
                new LocalizedTextResponse(category.getTitleAr(), category.getTitleEn()),
                new LocalizedTextResponse(category.getSummaryAr(), category.getSummaryEn()),
                category.getDisplayOrder(),
                entries,
                category.getUpdatedAt()
        );
    }
}
