package com.example.elhabashyback.about.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "about_certificates")
@Getter
@Setter
public class AboutCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_ar", nullable = false)
    private String titleAr;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "issuer_ar", nullable = false)
    private String issuerAr;

    @Column(name = "issuer_en", nullable = false)
    private String issuerEn;

    @Column(name = "description_ar", nullable = false, columnDefinition = "text")
    private String descriptionAr;

    @Column(name = "description_en", nullable = false, columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "issue_date")
    private LocalDate issueDate;

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
