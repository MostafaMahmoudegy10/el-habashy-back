package com.example.elhabashyback.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
public class AppSettings {

    @Id
    private Short id;

    @Column(name = "whatsapp_number", nullable = false, length = 40)
    private String whatsappNumber;

    @Column(name = "whatsapp_message_ar", nullable = false, columnDefinition = "text")
    private String whatsappMessageAr;

    @Column(name = "whatsapp_message_en", nullable = false, columnDefinition = "text")
    private String whatsappMessageEn;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", nullable = false, length = 320)
    private String contactEmail;

    @Column(name = "office_address_ar", nullable = false, columnDefinition = "text")
    private String officeAddressAr;

    @Column(name = "office_address_en", nullable = false, columnDefinition = "text")
    private String officeAddressEn;

    @Column(name = "map_url", nullable = false, columnDefinition = "text")
    private String mapUrl;

    @Column(name = "facebook_url", nullable = false, columnDefinition = "text")
    private String facebookUrl;

    @Column(name = "linkedin_url", nullable = false, columnDefinition = "text")
    private String linkedinUrl;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
