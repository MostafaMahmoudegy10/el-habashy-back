package com.example.elhabashyback.about.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "about_work_entries")
@Getter
@Setter
public class AboutWorkEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AboutWorkCategory category;

    @Column(name = "title_ar", nullable = false)
    private String titleAr;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "client_ar", nullable = false)
    private String clientAr;

    @Column(name = "client_en", nullable = false)
    private String clientEn;

    @Column(name = "summary_ar", nullable = false, columnDefinition = "text")
    private String summaryAr;

    @Column(name = "summary_en", nullable = false, columnDefinition = "text")
    private String summaryEn;

    @Column(name = "details_ar", nullable = false, columnDefinition = "text")
    private String detailsAr;

    @Column(name = "details_en", nullable = false, columnDefinition = "text")
    private String detailsEn;

    @Column(name = "project_year")
    private Integer projectYear;

    @Column(name = "location_ar", nullable = false)
    private String locationAr;

    @Column(name = "location_en", nullable = false)
    private String locationEn;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
