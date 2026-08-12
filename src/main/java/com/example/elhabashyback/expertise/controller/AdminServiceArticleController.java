package com.example.elhabashyback.expertise.controller;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.expertise.dto.ServiceArticleImageResponse;
import com.example.elhabashyback.expertise.dto.ServiceArticleResponse;
import com.example.elhabashyback.expertise.dto.UpsertServiceArticleRequest;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import com.example.elhabashyback.expertise.service.ServiceArticleImageService;
import com.example.elhabashyback.expertise.service.ServiceArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
public class AdminServiceArticleController {

    private final ServiceArticleService service;
    private final ServiceArticleImageService imageService;

    @GetMapping
    List<ServiceArticleResponse> list(@RequestParam(required = false) String kind) {
        return service.listAdmin(parseKind(kind));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServiceArticleResponse create(@Valid @RequestBody UpsertServiceArticleRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    ServiceArticleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpsertServiceArticleRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ServiceArticleImageResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return imageService.upload(file);
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
