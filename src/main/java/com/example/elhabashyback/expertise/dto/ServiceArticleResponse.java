package com.example.elhabashyback.expertise.dto;

import com.example.elhabashyback.common.dto.LocalizedTextResponse;
import com.example.elhabashyback.expertise.entity.ServiceArticle;
import com.example.elhabashyback.expertise.entity.ServiceKind;

import java.time.Instant;
import java.util.List;

public record ServiceArticleResponse(
        Long id,
        String slug,
        ServiceKind kind,
        LocalizedTextResponse title,
        LocalizedTextResponse summary,
        LocalizedTextResponse content,
        String image,
        List<String> gallery,
        boolean featured,
        Integer displayOrder,
        LocalizedTextResponse seoTitle,
        LocalizedTextResponse seoDescription,
        LocalizedTextResponse seoKeywords,
        Instant createdAt,
        Instant updatedAt
) {
    public static ServiceArticleResponse from(ServiceArticle article) {
        return new ServiceArticleResponse(
                article.getId(),
                article.getSlug(),
                article.getKind(),
                text(article.getTitleAr(), article.getTitleEn()),
                text(article.getSummaryAr(), article.getSummaryEn()),
                text(article.getContentAr(), article.getContentEn()),
                article.getHeroImageUrl(),
                article.getGallery().stream().map(image -> image.getImageUrl()).toList(),
                article.isFeatured(),
                article.getDisplayOrder(),
                optionalText(article.getSeoTitleAr(), article.getSeoTitleEn()),
                optionalText(article.getSeoDescriptionAr(), article.getSeoDescriptionEn()),
                optionalText(article.getSeoKeywordsAr(), article.getSeoKeywordsEn()),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }

    private static LocalizedTextResponse text(String ar, String en) {
        return new LocalizedTextResponse(ar, en);
    }

    private static LocalizedTextResponse optionalText(String ar, String en) {
        return ar == null && en == null ? null : text(ar, en);
    }
}
