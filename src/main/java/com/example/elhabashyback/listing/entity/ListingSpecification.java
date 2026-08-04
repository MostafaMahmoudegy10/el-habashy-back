package com.example.elhabashyback.listing.entity;

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

@Entity
@Table(name = "listing_specifications")
@Getter
@Setter
public class ListingSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "label_ar", nullable = false, columnDefinition = "text")
    private String labelAr;

    @Column(name = "label_en", nullable = false, columnDefinition = "text")
    private String labelEn;

    @Column(name = "value_ar", nullable = false, columnDefinition = "text")
    private String valueAr;

    @Column(name = "value_en", nullable = false, columnDefinition = "text")
    private String valueEn;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
