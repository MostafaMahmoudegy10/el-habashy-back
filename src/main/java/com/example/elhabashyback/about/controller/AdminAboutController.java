package com.example.elhabashyback.about.controller;

import com.example.elhabashyback.about.dto.AboutCertificateResponse;
import com.example.elhabashyback.about.dto.AboutDepartmentResponse;
import com.example.elhabashyback.about.dto.AboutImageResponse;
import com.example.elhabashyback.about.dto.AboutPersonResponse;
import com.example.elhabashyback.about.dto.AboutProfileResponse;
import com.example.elhabashyback.about.dto.AboutResponse;
import com.example.elhabashyback.about.dto.AboutWorkCategoryResponse;
import com.example.elhabashyback.about.dto.AboutWorkEntryResponse;
import com.example.elhabashyback.about.dto.UpdateAboutProfileRequest;
import com.example.elhabashyback.about.dto.UpsertAboutCertificateRequest;
import com.example.elhabashyback.about.dto.UpsertAboutDepartmentRequest;
import com.example.elhabashyback.about.dto.UpsertAboutPersonRequest;
import com.example.elhabashyback.about.dto.UpsertAboutWorkCategoryRequest;
import com.example.elhabashyback.about.dto.UpsertAboutWorkEntryRequest;
import com.example.elhabashyback.about.service.AboutImageService;
import com.example.elhabashyback.about.service.AboutService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/about")
@RequiredArgsConstructor
public class AdminAboutController {

    private final AboutService service;
    private final AboutImageService imageService;

    @GetMapping
    AboutResponse get() {
        return service.getAdminContent();
    }

    @PutMapping("/profile")
    AboutProfileResponse updateProfile(@Valid @RequestBody UpdateAboutProfileRequest request) {
        return service.updateProfile(request);
    }

    @PostMapping("/people")
    @ResponseStatus(HttpStatus.CREATED)
    AboutPersonResponse createPerson(@Valid @RequestBody UpsertAboutPersonRequest request) {
        return service.createPerson(request);
    }

    @PutMapping("/people/{id}")
    AboutPersonResponse updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAboutPersonRequest request
    ) {
        return service.updatePerson(id, request);
    }

    @DeleteMapping("/people/{id}")
    ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        service.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    AboutDepartmentResponse createDepartment(@Valid @RequestBody UpsertAboutDepartmentRequest request) {
        return service.createDepartment(request);
    }

    @PutMapping("/departments/{id}")
    AboutDepartmentResponse updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAboutDepartmentRequest request
    ) {
        return service.updateDepartment(id, request);
    }

    @DeleteMapping("/departments/{id}")
    ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        service.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    AboutCertificateResponse createCertificate(@Valid @RequestBody UpsertAboutCertificateRequest request) {
        return service.createCertificate(request);
    }

    @PutMapping("/certificates/{id}")
    AboutCertificateResponse updateCertificate(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAboutCertificateRequest request
    ) {
        return service.updateCertificate(id, request);
    }

    @DeleteMapping("/certificates/{id}")
    ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        service.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/work-categories")
    @ResponseStatus(HttpStatus.CREATED)
    AboutWorkCategoryResponse createWorkCategory(
            @Valid @RequestBody UpsertAboutWorkCategoryRequest request
    ) {
        return service.createWorkCategory(request);
    }

    @PutMapping("/work-categories/{id}")
    AboutWorkCategoryResponse updateWorkCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAboutWorkCategoryRequest request
    ) {
        return service.updateWorkCategory(id, request);
    }

    @DeleteMapping("/work-categories/{id}")
    ResponseEntity<Void> deleteWorkCategory(@PathVariable Long id) {
        service.deleteWorkCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/work-categories/{categoryId}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    AboutWorkEntryResponse createWorkEntry(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpsertAboutWorkEntryRequest request
    ) {
        return service.createWorkEntry(categoryId, request);
    }

    @PutMapping("/work-entries/{id}")
    AboutWorkEntryResponse updateWorkEntry(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAboutWorkEntryRequest request
    ) {
        return service.updateWorkEntry(id, request);
    }

    @DeleteMapping("/work-entries/{id}")
    ResponseEntity<Void> deleteWorkEntry(@PathVariable Long id) {
        service.deleteWorkEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AboutImageResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return imageService.upload(file);
    }
}
