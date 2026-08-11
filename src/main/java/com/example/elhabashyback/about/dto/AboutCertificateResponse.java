package com.example.elhabashyback.about.dto;

import com.example.elhabashyback.about.entity.AboutCertificate;
import com.example.elhabashyback.common.dto.LocalizedTextResponse;

import java.time.Instant;
import java.time.LocalDate;

public record AboutCertificateResponse(
        Long id,
        LocalizedTextResponse title,
        LocalizedTextResponse issuer,
        LocalizedTextResponse description,
        LocalDate issueDate,
        String imageUrl,
        Integer displayOrder,
        Instant updatedAt
) {
    public static AboutCertificateResponse from(AboutCertificate certificate) {
        return new AboutCertificateResponse(
                certificate.getId(),
                new LocalizedTextResponse(certificate.getTitleAr(), certificate.getTitleEn()),
                new LocalizedTextResponse(certificate.getIssuerAr(), certificate.getIssuerEn()),
                new LocalizedTextResponse(certificate.getDescriptionAr(), certificate.getDescriptionEn()),
                certificate.getIssueDate(),
                certificate.getImageUrl(),
                certificate.getDisplayOrder(),
                certificate.getUpdatedAt()
        );
    }
}
