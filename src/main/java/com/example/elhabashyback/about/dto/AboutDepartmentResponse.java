package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutDepartment;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;

public record AboutDepartmentResponse(
        Long id,
        LocalizedTextResponse title,
        LocalizedTextResponse description,
        Integer displayOrder,
        Instant updatedAt
) {
    public static AboutDepartmentResponse from(AboutDepartment department) {
        return new AboutDepartmentResponse(
                department.getId(),
                new LocalizedTextResponse(department.getTitleAr(), department.getTitleEn()),
                new LocalizedTextResponse(department.getDescriptionAr(), department.getDescriptionEn()),
                department.getDisplayOrder(),
                department.getUpdatedAt()
        );
    }
}
