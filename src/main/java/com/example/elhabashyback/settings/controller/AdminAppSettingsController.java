package com.example.elhabashyback.settings.controller;

import com.example.elhabashyback.settings.dto.AppSettingsResponse;
import com.example.elhabashyback.settings.dto.UpdateAppSettingsRequest;
import com.example.elhabashyback.settings.service.AppSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminAppSettingsController {

    private final AppSettingsService service;

    @GetMapping
    AppSettingsResponse get() {
        return service.get();
    }

    @PutMapping
    AppSettingsResponse update(@Valid @RequestBody UpdateAppSettingsRequest request) {
        return service.update(request);
    }
}
