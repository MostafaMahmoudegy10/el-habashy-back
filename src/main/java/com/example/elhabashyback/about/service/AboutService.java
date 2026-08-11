package com.example.elhabashyback.about.service;

import com.example.elhabashyback.about.dto.AboutCertificateResponse;
import com.example.elhabashyback.about.dto.AboutDepartmentResponse;
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
import com.example.elhabashyback.about.entity.AboutCertificate;
import com.example.elhabashyback.about.entity.AboutDepartment;
import com.example.elhabashyback.about.entity.AboutPerson;
import com.example.elhabashyback.about.entity.AboutProfile;
import com.example.elhabashyback.about.entity.AboutWorkCategory;
import com.example.elhabashyback.about.entity.AboutWorkEntry;
import com.example.elhabashyback.about.repository.AboutCertificateRepository;
import com.example.elhabashyback.about.repository.AboutDepartmentRepository;
import com.example.elhabashyback.about.repository.AboutPersonRepository;
import com.example.elhabashyback.about.repository.AboutProfileRepository;
import com.example.elhabashyback.about.repository.AboutWorkCategoryRepository;
import com.example.elhabashyback.about.repository.AboutWorkEntryRepository;
import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AboutService {

    private static final short PROFILE_ID = 1;

    private final AboutProfileRepository profileRepository;
    private final AboutPersonRepository personRepository;
    private final AboutDepartmentRepository departmentRepository;
    private final AboutCertificateRepository certificateRepository;
    private final AboutWorkCategoryRepository workCategoryRepository;
    private final AboutWorkEntryRepository workEntryRepository;

    @Transactional(readOnly = true)
    public AboutResponse getPublicContent() {
        return aggregate(false);
    }

    @Transactional(readOnly = true)
    public AboutResponse getAdminContent() {
        return aggregate(true);
    }

    @Transactional
    public AboutProfileResponse updateProfile(UpdateAboutProfileRequest request) {
        AboutProfile profile = profileRepository.findById(PROFILE_ID).orElseGet(this::defaultProfile);
        applyProfile(profile, request);
        return AboutProfileResponse.from(profileRepository.saveAndFlush(profile));
    }

    @Transactional
    public AboutPersonResponse createPerson(UpsertAboutPersonRequest request) {
        AboutPerson person = new AboutPerson();
        applyPerson(person, request);
        return AboutPersonResponse.from(personRepository.saveAndFlush(person));
    }

    @Transactional
    public AboutPersonResponse updatePerson(Long id, UpsertAboutPersonRequest request) {
        AboutPerson person = personRepository.findById(id)
                .orElseThrow(() -> notFound("Organizational person", id));
        applyPerson(person, request);
        return AboutPersonResponse.from(personRepository.saveAndFlush(person));
    }

    @Transactional
    public void deletePerson(Long id) {
        AboutPerson person = personRepository.findById(id)
                .orElseThrow(() -> notFound("Organizational person", id));
        personRepository.delete(person);
    }

    @Transactional
    public AboutDepartmentResponse createDepartment(UpsertAboutDepartmentRequest request) {
        AboutDepartment department = new AboutDepartment();
        applyDepartment(department, request);
        return AboutDepartmentResponse.from(departmentRepository.saveAndFlush(department));
    }

    @Transactional
    public AboutDepartmentResponse updateDepartment(Long id, UpsertAboutDepartmentRequest request) {
        AboutDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> notFound("Department", id));
        applyDepartment(department, request);
        return AboutDepartmentResponse.from(departmentRepository.saveAndFlush(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        AboutDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> notFound("Department", id));
        departmentRepository.delete(department);
    }

    @Transactional
    public AboutCertificateResponse createCertificate(UpsertAboutCertificateRequest request) {
        AboutCertificate certificate = new AboutCertificate();
        applyCertificate(certificate, request);
        return AboutCertificateResponse.from(certificateRepository.saveAndFlush(certificate));
    }

    @Transactional
    public AboutCertificateResponse updateCertificate(Long id, UpsertAboutCertificateRequest request) {
        AboutCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> notFound("Certificate", id));
        applyCertificate(certificate, request);
        return AboutCertificateResponse.from(certificateRepository.saveAndFlush(certificate));
    }

    @Transactional
    public void deleteCertificate(Long id) {
        AboutCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> notFound("Certificate", id));
        certificateRepository.delete(certificate);
    }

    @Transactional
    public AboutWorkCategoryResponse createWorkCategory(UpsertAboutWorkCategoryRequest request) {
        AboutWorkCategory category = new AboutWorkCategory();
        applyWorkCategory(category, request);
        AboutWorkCategory saved = workCategoryRepository.saveAndFlush(category);
        return AboutWorkCategoryResponse.from(saved, List.of());
    }

    @Transactional
    public AboutWorkCategoryResponse updateWorkCategory(Long id, UpsertAboutWorkCategoryRequest request) {
        AboutWorkCategory category = workCategoryRepository.findById(id)
                .orElseThrow(() -> notFound("Previous-work category", id));
        applyWorkCategory(category, request);
        AboutWorkCategory saved = workCategoryRepository.saveAndFlush(category);
        List<AboutWorkEntryResponse> entries = workEntryRepository
                .findByCategoryIdOrderByDisplayOrderAscIdAsc(id)
                .stream().map(AboutWorkEntryResponse::from).toList();
        return AboutWorkCategoryResponse.from(saved, entries);
    }

    @Transactional
    public void deleteWorkCategory(Long id) {
        AboutWorkCategory category = workCategoryRepository.findById(id)
                .orElseThrow(() -> notFound("Previous-work category", id));
        workEntryRepository.deleteAllByCategoryId(id);
        workCategoryRepository.delete(category);
        workCategoryRepository.flush();
    }

    @Transactional
    public AboutWorkEntryResponse createWorkEntry(Long categoryId, UpsertAboutWorkEntryRequest request) {
        AboutWorkCategory category = workCategoryRepository.findById(categoryId)
                .orElseThrow(() -> notFound("Previous-work category", categoryId));
        AboutWorkEntry entry = new AboutWorkEntry();
        entry.setCategory(category);
        applyWorkEntry(entry, request);
        return AboutWorkEntryResponse.from(workEntryRepository.saveAndFlush(entry));
    }

    @Transactional
    public AboutWorkEntryResponse updateWorkEntry(Long id, UpsertAboutWorkEntryRequest request) {
        AboutWorkEntry entry = workEntryRepository.findById(id)
                .orElseThrow(() -> notFound("Previous-work entry", id));
        applyWorkEntry(entry, request);
        return AboutWorkEntryResponse.from(workEntryRepository.saveAndFlush(entry));
    }

    @Transactional
    public void deleteWorkEntry(Long id) {
        AboutWorkEntry entry = workEntryRepository.findById(id)
                .orElseThrow(() -> notFound("Previous-work entry", id));
        workEntryRepository.delete(entry);
    }

    private AboutResponse aggregate(boolean includeInactivePeople) {
        AboutProfile profile = profileRepository.findById(PROFILE_ID).orElseGet(this::defaultProfile);
        List<AboutPerson> people = includeInactivePeople
                ? personRepository.findAllByOrderByDisplayOrderAscIdAsc()
                : personRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc();

        Map<Long, List<AboutWorkEntryResponse>> entriesByCategory = workEntryRepository
                .findAllByOrderByCategoryDisplayOrderAscDisplayOrderAscIdAsc()
                .stream()
                .map(AboutWorkEntryResponse::from)
                .collect(Collectors.groupingBy(
                        AboutWorkEntryResponse::categoryId,
                        Collectors.toList()
                ));

        return new AboutResponse(
                AboutProfileResponse.from(profile),
                people.stream().map(AboutPersonResponse::from).toList(),
                departmentRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                        .map(AboutDepartmentResponse::from).toList(),
                certificateRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                        .map(AboutCertificateResponse::from).toList(),
                workCategoryRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                        .map(category -> AboutWorkCategoryResponse.from(
                                category,
                                entriesByCategory.getOrDefault(category.getId(), List.of())
                        ))
                        .toList()
        );
    }

    private void applyProfile(AboutProfile profile, UpdateAboutProfileRequest request) {
        profile.setId(PROFILE_ID);
        profile.setHeadlineAr(ar(request.headline()));
        profile.setHeadlineEn(en(request.headline()));
        profile.setProfileAr(ar(request.profile()));
        profile.setProfileEn(en(request.profile()));
        profile.setMissionAr(ar(request.mission()));
        profile.setMissionEn(en(request.mission()));
        profile.setVisionAr(ar(request.vision()));
        profile.setVisionEn(en(request.vision()));
        profile.setProfileImageUrl(trimToNull(request.imageUrl()));
        profile.setStartedYear(request.startedYear());
    }

    private void applyPerson(AboutPerson person, UpsertAboutPersonRequest request) {
        person.setNameAr(ar(request.name()));
        person.setNameEn(en(request.name()));
        person.setRoleAr(ar(request.role()));
        person.setRoleEn(en(request.role()));
        person.setBiographyAr(ar(request.biography()));
        person.setBiographyEn(en(request.biography()));
        person.setImageUrl(trimToNull(request.imageUrl()));
        person.setDisplayOrder(request.displayOrder());
        person.setActive(request.active());
    }

    private void applyDepartment(AboutDepartment department, UpsertAboutDepartmentRequest request) {
        department.setTitleAr(ar(request.title()));
        department.setTitleEn(en(request.title()));
        department.setDescriptionAr(ar(request.description()));
        department.setDescriptionEn(en(request.description()));
        department.setDisplayOrder(request.displayOrder());
    }

    private void applyCertificate(AboutCertificate certificate, UpsertAboutCertificateRequest request) {
        certificate.setTitleAr(ar(request.title()));
        certificate.setTitleEn(en(request.title()));
        certificate.setIssuerAr(ar(request.issuer()));
        certificate.setIssuerEn(en(request.issuer()));
        certificate.setDescriptionAr(ar(request.description()));
        certificate.setDescriptionEn(en(request.description()));
        certificate.setIssueDate(request.issueDate());
        certificate.setImageUrl(trimToNull(request.imageUrl()));
        certificate.setDisplayOrder(request.displayOrder());
    }

    private void applyWorkCategory(AboutWorkCategory category, UpsertAboutWorkCategoryRequest request) {
        category.setTitleAr(ar(request.title()));
        category.setTitleEn(en(request.title()));
        category.setSummaryAr(ar(request.summary()));
        category.setSummaryEn(en(request.summary()));
        category.setDisplayOrder(request.displayOrder());
    }

    private void applyWorkEntry(AboutWorkEntry entry, UpsertAboutWorkEntryRequest request) {
        entry.setTitleAr(ar(request.title()));
        entry.setTitleEn(en(request.title()));
        entry.setClientAr(ar(request.client()));
        entry.setClientEn(en(request.client()));
        entry.setSummaryAr(ar(request.summary()));
        entry.setSummaryEn(en(request.summary()));
        entry.setDetailsAr(ar(request.details()));
        entry.setDetailsEn(en(request.details()));
        entry.setProjectYear(request.projectYear());
        entry.setLocationAr(ar(request.location()));
        entry.setLocationEn(en(request.location()));
        entry.setImageUrl(trimToNull(request.imageUrl()));
        entry.setDisplayOrder(request.displayOrder());
    }

    private AboutProfile defaultProfile() {
        AboutProfile profile = new AboutProfile();
        profile.setId(PROFILE_ID);
        profile.setHeadlineAr("خبرة ممتدة في التثمين وإدارة المزادات منذ عام 1944");
        profile.setHeadlineEn("Valuation and auction expertise since 1944");
        profile.setProfileAr("الحبشي للخبراء المثمنين للخبرة والتثمين، خبرة مصرية ممتدة في تقييم الأصول وإدارة المزادات والخبرة الفنية.");
        profile.setProfileEn("El Habashy Valuation Experts for Expertise and Appraisal is a long-established Egyptian practice in asset valuation, auctions and technical expertise.");
        profile.setMissionAr("تقديم تقييمات وخبرات فنية مستقلة وإدارة المزادات بصورة منظمة وشفافة.");
        profile.setMissionEn("To deliver independent valuation and technical expertise through transparent, organized processes.");
        profile.setVisionAr("أن تظل الحبشي مرجعًا موثوقًا للخبرة والتثمين في مصر.");
        profile.setVisionEn("To remain a trusted Egyptian reference for expertise and appraisal.");
        profile.setStartedYear(1944);
        return profile;
    }

    private ResourceNotFoundException notFound(String resource, Long id) {
        return new ResourceNotFoundException(resource + " with id " + id + " was not found");
    }

    private String ar(LocalizedTextRequest value) {
        return value.ar().trim();
    }

    private String en(LocalizedTextRequest value) {
        return value.en().trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
