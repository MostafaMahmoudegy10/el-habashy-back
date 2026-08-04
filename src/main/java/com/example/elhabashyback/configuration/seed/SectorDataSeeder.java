package com.example.elhabashyback.configuration.seed;

import com.example.elhabashyback.sector.entity.Sector;
import com.example.elhabashyback.sector.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(20)
public class SectorDataSeeder implements ApplicationRunner {

    private static final List<SectorSeed> SECTORS = List.of(
            new SectorSeed(
                    "real-estate", 0,
                    "العقارات", "Real Estate",
                    "أراضي، وحدات، فيلات، ومبان جاهزة للمعاينة والتواصل.",
                    "Land, units, villas, and buildings prepared for viewing and contact."),
            new SectorSeed(
                    "movables", 1,
                    "العدد والمنقولات", "Movables",
                    "معدات، مخزون، أجهزة، ومنقولات تشغيلية قابلة للفحص.",
                    "Equipment, inventory, devices, and operational movables ready for inspection."),
            new SectorSeed(
                    "cars", 2,
                    "السيارات", "Cars",
                    "سيارات ملاكي، نقل، وأساطيل شركات مع بيانات واضحة.",
                    "Passenger cars, transport vehicles, and company fleets with clear data."),
            new SectorSeed(
                    "antiques", 3,
                    "التحف والأنتيكات", "Antiques",
                    "قطع فنية، ديكور، مقتنيات، ومجموعات كلاسيكية.",
                    "Art pieces, decor, collectibles, and classic collections."),
            new SectorSeed(
                    "scrap", 4,
                    "المخلفات والسكراب", "Scrap",
                    "مخلفات تشغيل، خردة، وسكراب صناعي حسب الوزن والمعاينة.",
                    "Operational leftovers, scrap, and industrial lots by weight and inspection."),
            new SectorSeed(
                    "other", 5,
                    "أخرى", "Other",
                    "أي عروض أو أصول لا تدخل تحت القطاعات الأساسية.",
                    "Any listings or assets outside the main sectors.")
    );

    private final SectorRepository sectorRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Sector> missingSectors = SECTORS.stream()
                .filter(seed -> !sectorRepository.existsById(seed.code()))
                .map(this::toEntity)
                .toList();

        if (!missingSectors.isEmpty()) {
            sectorRepository.saveAll(missingSectors);
        }
    }

    private Sector toEntity(SectorSeed seed) {
        Sector sector = new Sector();
        sector.setCode(seed.code());
        sector.setDisplayOrder(seed.displayOrder());
        sector.setTitleAr(seed.titleAr());
        sector.setTitleEn(seed.titleEn());
        sector.setDescriptionAr(seed.descriptionAr());
        sector.setDescriptionEn(seed.descriptionEn());
        return sector;
    }

    private record SectorSeed(
            String code,
            int displayOrder,
            String titleAr,
            String titleEn,
            String descriptionAr,
            String descriptionEn
    ) {
    }
}
