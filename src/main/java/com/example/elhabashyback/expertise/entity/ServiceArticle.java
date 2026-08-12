package com.example.elhabashyback.expertise.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "service_articles",
        indexes = {
                @Index(name = "idx_service_articles_kind_order", columnList = "kind,display_order,id"),
                @Index(name = "idx_service_articles_featured", columnList = "featured")
        }
)
@Getter
@Setter
public class ServiceArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceKind kind;

    @Column(name = "title_ar", nullable = false, columnDefinition = "text")
    private String titleAr;

    @Column(name = "title_en", nullable = false, columnDefinition = "text")
    private String titleEn;

    @Column(name = "summary_ar", nullable = false, columnDefinition = "text")
    private String summaryAr;

    @Column(name = "summary_en", nullable = false, columnDefinition = "text")
    private String summaryEn;

    @Column(name = "content_ar", nullable = false, columnDefinition = "text")
    private String contentAr;

    @Column(name = "content_en", nullable = false, columnDefinition = "text")
    private String contentEn;

    @Column(name = "hero_image_url", nullable = false, columnDefinition = "text")
    private String heroImageUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "seo_title_ar", columnDefinition = "text")
    private String seoTitleAr;

    @Column(name = "seo_title_en", columnDefinition = "text")
    private String seoTitleEn;

    @Column(name = "seo_description_ar", columnDefinition = "text")
    private String seoDescriptionAr;

    @Column(name = "seo_description_en", columnDefinition = "text")
    private String seoDescriptionEn;

    @Column(name = "seo_keywords_ar", columnDefinition = "text")
    private String seoKeywordsAr;

    @Column(name = "seo_keywords_en", columnDefinition = "text")
    private String seoKeywordsEn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ServiceArticleGalleryImage> gallery = new ArrayList<>();

    public void replaceGallery(List<String> imageUrls) {
        gallery.clear();
        for (int index = 0; index < imageUrls.size(); index++) {
            ServiceArticleGalleryImage image = new ServiceArticleGalleryImage();
            image.setArticle(this);
            image.setImageUrl(imageUrls.get(index));
            image.setDisplayOrder(index);
            gallery.add(image);
        }
    }
}
