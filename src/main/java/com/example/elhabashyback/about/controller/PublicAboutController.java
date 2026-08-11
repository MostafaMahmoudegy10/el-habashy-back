package com.example.elhabashyback.about.controller;

import com.example.elhabashyback.about.dto.AboutResponse;
import com.example.elhabashyback.about.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/about")
@RequiredArgsConstructor
public class PublicAboutController {

    private final AboutService service;

    @GetMapping
    AboutResponse get() {
        return service.getPublicContent();
    }
}
