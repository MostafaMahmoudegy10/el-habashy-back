package com.example.elhabashyback.settings.dto;

import com.example.elhabashyback.common.dto.LocalizedTextResponse;
import com.example.elhabashyback.settings.entity.AppSettings;

import java.time.Instant;

public record AppSettingsResponse(
        String whatsappNumber,
        String whatsappMessageAr,
        String whatsappMessageEn,
        String contactPhone,
        String contactEmail,
        LocalizedTextResponse officeAddress,
        String mapUrl,
        String facebookUrl,
        String linkedinUrl,
        Instant updatedAt
) {
    public static AppSettingsResponse from(AppSettings settings) {
        return new AppSettingsResponse(
                settings.getWhatsappNumber(),
                settings.getWhatsappMessageAr(),
                settings.getWhatsappMessageEn(),
                settings.getContactPhone(),
                settings.getContactEmail(),
                new LocalizedTextResponse(settings.getOfficeAddressAr(), settings.getOfficeAddressEn()),
                settings.getMapUrl(),
                settings.getFacebookUrl(),
                settings.getLinkedinUrl(),
                settings.getUpdatedAt()
        );
    }
}
