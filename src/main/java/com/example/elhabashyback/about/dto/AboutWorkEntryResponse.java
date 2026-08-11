package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutWorkEntry;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;

public record AboutWorkEntryResponse(
        Long id,
        Long categoryId,
        LocalizedTextResponse title,
        LocalizedTextResponse client,
        LocalizedTextResponse summary,
        LocalizedTextResponse details,
        Integer projectYear,
        LocalizedTextResponse location,
        String imageUrl,
        Integer displayOrder,
        Instant updatedAt
) {
    public static AboutWorkEntryResponse from(AboutWorkEntry entry) {
        return new AboutWorkEntryResponse(
                entry.getId(),
                entry.getCategory().getId(),
                new LocalizedTextResponse(entry.getTitleAr(), entry.getTitleEn()),
                new LocalizedTextResponse(entry.getClientAr(), entry.getClientEn()),
                new LocalizedTextResponse(entry.getSummaryAr(), entry.getSummaryEn()),
                new LocalizedTextResponse(entry.getDetailsAr(), entry.getDetailsEn()),
                entry.getProjectYear(),
                new LocalizedTextResponse(entry.getLocationAr(), entry.getLocationEn()),
                entry.getImageUrl(),
                entry.getDisplayOrder(),
                entry.getUpdatedAt()
        );
    }
}
