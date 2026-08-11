package com.example.elhabashyback.about.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "about_profile")
@Getter
@Setter
public class AboutProfile {

    @Id
    private Short id;

    @Column(name = "headline_ar", nullable = false, columnDefinition = "text")
    private String headlineAr;

    @Column(name = "headline_en", nullable = false, columnDefinition = "text")
    private String headlineEn;

    @Column(name = "profile_ar", nullable = false, columnDefinition = "text")
    private String profileAr;

    @Column(name = "profile_en", nullable = false, columnDefinition = "text")
    private String profileEn;

    @Column(name = "mission_ar", nullable = false, columnDefinition = "text")
    private String missionAr;

    @Column(name = "mission_en", nullable = false, columnDefinition = "text")
    private String missionEn;

    @Column(name = "vision_ar", nullable = false, columnDefinition = "text")
    private String visionAr;

    @Column(name = "vision_en", nullable = false, columnDefinition = "text")
    private String visionEn;

    @Column(name = "profile_image_url", columnDefinition = "text")
    private String profileImageUrl;

    @Column(name = "started_year", nullable = false)
    private Integer startedYear;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
