package com.example.elhabashyback.settings.controller;

import com.example.elhabashyback.settings.dto.AppSettingsResponse;
import com.example.elhabashyback.settings.service.AppSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/settings")
@RequiredArgsConstructor
public class PublicAppSettingsController {

    private final AppSettingsService service;

    @GetMapping
    AppSettingsResponse get() {
        return service.get();
    }
}
