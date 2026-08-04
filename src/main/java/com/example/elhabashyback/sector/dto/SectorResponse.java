package com.example.elhabashyback.sector.dto;

import com.example.elhabashyback.common.dto.LocalizedTextResponse;
import com.example.elhabashyback.sector.entity.Sector;

import java.time.Instant;

public record SectorResponse(
        String code,
        int displayOrder,
        LocalizedTextResponse title,
        LocalizedTextResponse description,
        Instant updatedAt
) {
    public static SectorResponse from(Sector sector) {
        return new SectorResponse(
                sector.getCode(),
                sector.getDisplayOrder(),
                new LocalizedTextResponse(sector.getTitleAr(), sector.getTitleEn()),
                new LocalizedTextResponse(sector.getDescriptionAr(), sector.getDescriptionEn()),
                sector.getUpdatedAt()
        );
    }
}
