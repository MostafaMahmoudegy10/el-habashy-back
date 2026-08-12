package com.example.elhabashyback.expertise.service;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.configuration.cache.CacheConfiguration;
import com.example.elhabashyback.expertise.dto.ServiceArticleResponse;
import com.example.elhabashyback.expertise.dto.UpsertServiceArticleRequest;
import com.example.elhabashyback.expertise.entity.ServiceArticle;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import com.example.elhabashyback.expertise.repository.ServiceArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ServiceArticleService {

    private final ServiceArticleRepository repository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfiguration.PUBLIC_SERVICES, key = "#kind == null ? 'all' : #kind.name()", sync = true)
    public List<ServiceArticleResponse> listPublic(ServiceKind kind) {
        return list(kind);
    }

    @Transactional(readOnly = true)
    public List<ServiceArticleResponse> listAdmin(ServiceKind kind) {
        return list(kind);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfiguration.PUBLIC_SERVICES, key = "'slug:' + #slug.toLowerCase()", sync = true)
    public ServiceArticleResponse getPublic(String slug) {
        return ServiceArticleResponse.from(repository.findBySlugIgnoreCase(slug.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Service article not found")));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_SERVICES, allEntries = true)
    public ServiceArticleResponse create(UpsertServiceArticleRequest request) {
        ServiceArticle article = new ServiceArticle();
        article.setSlug(uniqueSlug(request.title().en()));
        apply(article, request);
        return ServiceArticleResponse.from(repository.saveAndFlush(article));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_SERVICES, allEntries = true)
    public ServiceArticleResponse update(Long id, UpsertServiceArticleRequest request) {
        ServiceArticle article = get(id);
        apply(article, request);
        repository.flush();
        return ServiceArticleResponse.from(article);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_SERVICES, allEntries = true)
    public void delete(Long id) {
        repository.delete(get(id));
        repository.flush();
    }

    private List<ServiceArticleResponse> list(ServiceKind kind) {
        List<ServiceArticle> articles = kind == null
                ? repository.findAllByOrderByDisplayOrderAscIdAsc()
                : repository.findByKindOrderByDisplayOrderAscIdAsc(kind);
        return articles.stream().map(ServiceArticleResponse::from).toList();
    }

    private ServiceArticle get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service article not found"));
    }

    private void apply(ServiceArticle article, UpsertServiceArticleRequest request) {
        article.setKind(request.kind());
        article.setTitleAr(trim(request.title().ar()));
        article.setTitleEn(trim(request.title().en()));
        article.setSummaryAr(trim(request.summary().ar()));
        article.setSummaryEn(trim(request.summary().en()));
        article.setContentAr(trim(request.content().ar()));
        article.setContentEn(trim(request.content().en()));
        article.setHeroImageUrl(trim(request.image()));
        article.setFeatured(request.featured());
        article.setDisplayOrder(request.displayOrder());
        article.replaceGallery(request.gallery().stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList());
        setOptionalText(article, request.seoTitle(), OptionalTextTarget.TITLE);
        setOptionalText(article, request.seoDescription(), OptionalTextTarget.DESCRIPTION);
        setOptionalText(article, request.seoKeywords(), OptionalTextTarget.KEYWORDS);
    }

    private void setOptionalText(ServiceArticle article, LocalizedTextRequest text, OptionalTextTarget target) {
        String ar = text == null ? null : trim(text.ar());
        String en = text == null ? null : trim(text.en());
        switch (target) {
            case TITLE -> {
                article.setSeoTitleAr(ar);
                article.setSeoTitleEn(en);
            }
            case DESCRIPTION -> {
                article.setSeoDescriptionAr(ar);
                article.setSeoDescriptionEn(en);
            }
            case KEYWORDS -> {
                article.setSeoKeywordsAr(ar);
                article.setSeoKeywordsEn(en);
            }
        }
    }

    private String uniqueSlug(String title) {
        String base = normalizeSlug(title);
        if (base.isBlank()) {
            base = "service";
        }
        String candidate = base;
        int suffix = 2;
        while (repository.existsBySlugIgnoreCase(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String normalizeSlug(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String trim(String value) {
        return value.trim();
    }

    private enum OptionalTextTarget {
        TITLE,
        DESCRIPTION,
        KEYWORDS
    }
}
