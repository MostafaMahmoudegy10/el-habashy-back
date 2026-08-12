package com.example.elhabashyback.expertise.controller;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.expertise.dto.ServiceArticleResponse;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import com.example.elhabashyback.expertise.service.ServiceArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/services")
@RequiredArgsConstructor
public class PublicServiceArticleController {

    private final ServiceArticleService service;

    @GetMapping
    List<ServiceArticleResponse> list(@RequestParam(required = false) String kind) {
        return service.listPublic(parseKind(kind));
    }

    @GetMapping("/{slug}")
    ServiceArticleResponse get(@PathVariable String slug) {
        return service.getPublic(slug);
    }

    private ServiceKind parseKind(String kind) {
        if (kind == null || kind.isBlank()) {
            return null;
        }
        try {
            return ServiceKind.fromValue(kind);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
