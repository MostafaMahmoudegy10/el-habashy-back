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

@Entity
@Table(name = "about_work_categories")
@Getter
@Setter
public class AboutWorkCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_ar", nullable = false)
    private String titleAr;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "summary_ar", nullable = false, columnDefinition = "text")
    private String summaryAr;

    @Column(name = "summary_en", nullable = false, columnDefinition = "text")
    private String summaryEn;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
