package com.example.elhabashyback.sector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "sectors",
        uniqueConstraints = @UniqueConstraint(name = "uk_sectors_display_order", columnNames = "display_order")
)
@Getter
@Setter
public class Sector {

    @Id
    @Column(name = "code", length = 50, nullable = false, updatable = false)
    private String code;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "title_ar", nullable = false)
    private String titleAr;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "description_ar", nullable = false, columnDefinition = "text")
    private String descriptionAr;

    @Column(name = "description_en", nullable = false, columnDefinition = "text")
    private String descriptionEn;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
