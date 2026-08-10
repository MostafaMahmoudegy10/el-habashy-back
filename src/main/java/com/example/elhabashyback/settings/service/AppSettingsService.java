package com.example.elhabashyback.settings.service;

import com.example.elhabashyback.settings.dto.AppSettingsResponse;
import com.example.elhabashyback.settings.dto.UpdateAppSettingsRequest;
import com.example.elhabashyback.settings.entity.AppSettings;
import com.example.elhabashyback.settings.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppSettingsService {

    private static final short SETTINGS_ID = 1;

    private final AppSettingsRepository repository;

    @Transactional
    public AppSettingsResponse get() {
        return AppSettingsResponse.from(repository.findById(SETTINGS_ID).orElseGet(this::createDefaults));
    }

    @Transactional
    public AppSettingsResponse update(UpdateAppSettingsRequest request) {
        AppSettings settings = repository.findById(SETTINGS_ID).orElseGet(this::defaults);
        settings.setWhatsappNumber(request.whatsappNumber().trim());
        settings.setWhatsappMessageAr(request.whatsappMessageAr().trim());
        settings.setWhatsappMessageEn(request.whatsappMessageEn().trim());
        settings.setContactPhone(request.contactPhone().trim());
        settings.setContactEmail(request.contactEmail().trim().toLowerCase());
        settings.setOfficeAddressAr(request.officeAddress().ar().trim());
        settings.setOfficeAddressEn(request.officeAddress().en().trim());
        settings.setMapUrl(request.mapUrl().trim());
        settings.setFacebookUrl(trimOrEmpty(request.facebookUrl()));
        settings.setLinkedinUrl(trimOrEmpty(request.linkedinUrl()));
        return AppSettingsResponse.from(repository.saveAndFlush(settings));
    }

    private AppSettings createDefaults() {
        return repository.saveAndFlush(defaults());
    }

    private AppSettings defaults() {
        AppSettings settings = new AppSettings();
        settings.setId(SETTINGS_ID);
        settings.setWhatsappNumber("201000000000");
        settings.setWhatsappMessageAr("أهلا، أحتاج تفاصيل المزاد الخاصة بـ {title}");
        settings.setWhatsappMessageEn("Hello, I need the auction details for {title}");
        settings.setContactPhone("25789288 - 202 / 25780424 -202 / 25780425 -202");
        settings.setContactEmail("info@elhabashy.com");
        settings.setOfficeAddressAr("22 ش محمود بسيوني - قصر النيل - القاهرة");
        settings.setOfficeAddressEn("22 Mahmoud Bassiouny St. - Kasr El Nil - Cairo");
        settings.setMapUrl("https://maps.google.com/?q=22%20Mahmoud%20Bassiouny%20St%20Kasr%20El%20Nil%20Cairo");
        settings.setFacebookUrl("https://www.facebook.com/elhabashy.auctionappraisal/");
        settings.setLinkedinUrl("https://www.linkedin.com/company/elhabashy/");
        return settings;
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
