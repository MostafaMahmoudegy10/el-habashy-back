package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutPerson;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;

public record AboutPersonResponse(
        Long id,
        LocalizedTextResponse name,
        LocalizedTextResponse role,
        LocalizedTextResponse biography,
        String imageUrl,
        Integer displayOrder,
        Boolean active,
        Instant updatedAt
) {
    public static AboutPersonResponse from(AboutPerson person) {
        return new AboutPersonResponse(
                person.getId(),
                new LocalizedTextResponse(person.getNameAr(), person.getNameEn()),
                new LocalizedTextResponse(person.getRoleAr(), person.getRoleEn()),
                new LocalizedTextResponse(person.getBiographyAr(), person.getBiographyEn()),
                person.getImageUrl(),
                person.getDisplayOrder(),
                person.getActive(),
                person.getUpdatedAt()
        );
    }
}
