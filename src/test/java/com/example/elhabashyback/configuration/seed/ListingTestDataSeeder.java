package com.example.elhabashyback.configuration.seed;

import com.example.elhabashyback.listing.entity.Listing;
import com.example.elhabashyback.listing.entity.ListingSpecification;
import com.example.elhabashyback.listing.entity.ListingStatus;
import com.example.elhabashyback.listing.repository.ListingRepository;
import com.example.elhabashyback.sector.entity.Sector;
import com.example.elhabashyback.sector.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Test-only fixture. Production must start with no seeded listings.
@Component
@RequiredArgsConstructor
@Order(30)
public class ListingTestDataSeeder implements ApplicationRunner {

    private static final List<ListingSeed> LISTINGS = List.of(
            new ListingSeed("new-cairo-private-villa", "فيلا مستقلة بحديقة خاصة في التجمع الخامس", "Standalone Villa With Private Garden in New Cairo", "عقار سكني مميز جاهز للمعاينة مع مستندات واضحة.", "A premium residential asset prepared for inspection.", "real-estate", ListingStatus.ACTIVE, "القاهرة الجديدة", "New Cairo", "18.5 مليون جنيه", "EGP 18.5M", "420 m²", true),
            new ListingSeed("october-residential-land", "قطعة أرض سكنية مميزة بمدينة السادس من أكتوبر", "Residential Land in 6th of October City", "قطعة أرض بموقع مميز وقريبة من المحاور الرئيسية.", "A well-located residential land plot near main roads.", "real-estate", ListingStatus.CLOSED, "السادس من أكتوبر", "6th of October", "حسب كراسة الشروط", "According to auction booklet", "1,250 m²", false),
            new ListingSeed("alexandria-commercial-building", "مبنى تجاري وإداري وسط الإسكندرية", "Commercial and Administrative Building in Alexandria", "مبنى متعدد الاستخدامات في منطقة تجارية نشطة.", "A mixed-use building in an active commercial district.", "real-estate", ListingStatus.COMING_SOON, "الإسكندرية", "Alexandria", "السعر عند التواصل", "Contact for price", "2,100 m²", true),
            new ListingSeed("company-car-fleet", "مجموعة سيارات شركة بحالة تشغيل جيدة", "Company Car Fleet in Good Running Condition", "أسطول سيارات ملاكي ونقل خفيف مع بيانات تفصيلية.", "Passenger and light transport fleet with detailed records.", "cars", ListingStatus.ACTIVE, "مدينة نصر", "Nasr City", "حسب كل سيارة", "Per vehicle", "12 vehicles", true),
            new ListingSeed("bank-repossessed-vehicles", "سيارات متنوعة لصالح أحد البنوك", "Mixed Vehicle Auction for a Major Bank", "سيارات موديلات مختلفة متاحة للفحص قبل جلسة المزاد.", "Multiple vehicle models available for pre-auction inspection.", "cars", ListingStatus.ACTIVE, "القاهرة", "Cairo", "مزاد علني", "Public auction", "24 vehicles", false),
            new ListingSeed("transport-vehicles", "سيارات نقل ومقطورات تشغيلية", "Operational Trucks and Trailers", "مجموعة نقل ثقيل ومقطورات بحالات تشغيل متنوعة.", "Heavy transport vehicles and trailers in varied conditions.", "cars", ListingStatus.INACTIVE, "السويس", "Suez", "حسب المعاينة", "Based on inspection", "9 vehicles", false),
            new ListingSeed("classic-antiques-collection", "مجموعة أنتيكات وتحف كلاسيكية", "Classic Antiques and Collectibles Collection", "قطع ديكور ونحاسيات وأثاث كلاسيكي لهواة المقتنيات.", "Decor, brass pieces, and classic furniture for collectors.", "antiques", ListingStatus.COMING_SOON, "الإسكندرية", "Alexandria", "تسعير حسب القطعة", "Priced per item", "34 pieces", true),
            new ListingSeed("heritage-furniture-auction", "مزاد أثاث تراثي ومقتنيات فنية", "Heritage Furniture and Art Auction", "أثاث خشبي ومقتنيات فنية موثقة بالصور.", "Wooden furniture and art pieces documented with photos.", "antiques", ListingStatus.ACTIVE, "الزمالك", "Zamalek", "مزاد علني", "Public auction", "51 pieces", true),
            new ListingSeed("brass-collectibles", "مجموعة نحاسيات ومقتنيات قديمة", "Brass and Vintage Collectibles", "مجموعة مختارة من النحاسيات والقطع القديمة.", "A selected collection of brass and vintage items.", "antiques", ListingStatus.CLOSED, "الجيزة", "Giza", "تم إغلاق المزاد", "Auction closed", "27 pieces", false),
            new ListingSeed("factory-metal-scrap", "مخلفات معدنية وسكراب من مصنع", "Factory Metal Scrap and Industrial Leftovers", "مخلفات تشغيل معدنية مصنفة مناسبة للتجار والمصانع.", "Sorted metal leftovers for traders and industrial operators.", "scrap", ListingStatus.ACTIVE, "العاشر من رمضان", "10th of Ramadan", "حسب الوزن والمعاينة", "By weight and inspection", "48 tons", false),
            new ListingSeed("aluminum-scrap-lot", "لوط سكراب ألومنيوم وفرز صناعي", "Aluminum Scrap and Industrial Sorting Lot", "كميات ألومنيوم مصنفة ومتاحة للمعاينة بالموقع.", "Sorted aluminum quantities available for site inspection.", "scrap", ListingStatus.COMING_SOON, "السادات", "Sadat City", "بالطن", "Per ton", "31 tons", true),
            new ListingSeed("cable-scrap", "كابلات وخردة نحاس متنوعة", "Cable and Copper Scrap", "كابلات تشغيل وخردة نحاس تباع كلوط واحد.", "Operational cables and copper scrap sold as one lot.", "scrap", ListingStatus.INACTIVE, "حلوان", "Helwan", "حسب الوزن", "By weight", "16 tons", false),
            new ListingSeed("factory-machinery", "خط إنتاج ومعدات مصنع", "Factory Production Line and Machinery", "معدات تشغيل وخط إنتاج متكامل متاح للفحص الفني.", "Machinery and a complete production line ready for inspection.", "movables", ListingStatus.ACTIVE, "برج العرب", "Borg El Arab", "طبقًا لكراسة الشروط", "Per auction booklet", "18 machines", true),
            new ListingSeed("hotel-furniture", "أثاث وتجهيزات فندق", "Hotel Furniture and Equipment", "غرف وأثاث وتجهيزات فندقية بحالة جيدة.", "Hotel rooms, furniture, and equipment in good condition.", "movables", ListingStatus.ACTIVE, "الغردقة", "Hurghada", "باللوط", "Per lot", "120 items", false),
            new ListingSeed("warehouse-equipment", "معدات مخازن ورافعات شوكية", "Warehouse Equipment and Forklifts", "معدات مناولة ورفوف ورافعات شوكية متنوعة.", "Material-handling equipment, racks, and forklifts.", "movables", ListingStatus.CLOSED, "العبور", "Obour City", "تم البيع", "Sold", "42 items", false),
            new ListingSeed("medical-equipment", "أجهزة ومعدات طبية متنوعة", "Mixed Medical Equipment", "أجهزة عيادات ومعدات طبية متاحة للفحص الفني.", "Clinic devices and medical equipment ready for technical inspection.", "other", ListingStatus.ACTIVE, "الدقي", "Dokki", "حسب الجهاز", "Per device", "36 devices", true),
            new ListingSeed("office-assets", "أثاث مكتبي وأجهزة إدارية", "Office Furniture and Administrative Equipment", "مكاتب وكراسي وأجهزة إدارية ضمن عدة لوطات.", "Desks, chairs, and office devices arranged in multiple lots.", "other", ListingStatus.COMING_SOON, "المعادي", "Maadi", "باللوط", "Per lot", "85 items", false),
            new ListingSeed("restaurant-equipment", "معدات وتجهيزات مطعم", "Restaurant Equipment and Fixtures", "معدات مطبخ وتجهيزات تشغيل كاملة لمطعم.", "Kitchen machines and complete restaurant operating equipment.", "other", ListingStatus.INACTIVE, "مصر الجديدة", "Heliopolis", "حسب المعاينة", "Based on inspection", "63 items", false)
    );

    private final ListingRepository listingRepository;
    private final SectorRepository sectorRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (int index = 0; index < LISTINGS.size(); index++) {
            ListingSeed seed = LISTINGS.get(index);
            if (!listingRepository.existsBySlugIgnoreCase(seed.slug())) {
                listingRepository.save(toEntity(seed, index));
            }
        }
    }

    private Listing toEntity(ListingSeed seed, int index) {
        Sector sector = sectorRepository.findById(seed.sectorCode()).orElseThrow();
        LocalDate publishDate = LocalDate.of(2026, 7, 1).plusDays(index);

        Listing listing = new Listing();
        listing.setSlug(seed.slug());
        listing.setSector(sector);
        listing.setStatus(seed.status());
        listing.setTitleAr(seed.titleAr());
        listing.setTitleEn(seed.titleEn());
        listing.setSummaryAr(seed.summaryAr());
        listing.setSummaryEn(seed.summaryEn());
        listing.setDescriptionAr("<h2>تفاصيل العرض</h2><p>" + seed.summaryAr() + "</p><p>المعاينة متاحة وفق المواعيد والشروط المعلنة.</p>");
        listing.setDescriptionEn("<h2>Listing details</h2><p>" + seed.summaryEn() + "</p><p>Inspection is available according to the announced schedule and terms.</p>");
        listing.setCityAr(seed.cityAr());
        listing.setCityEn(seed.cityEn());
        listing.setLocationAr(seed.cityAr() + " - موقع المعاينة");
        listing.setLocationEn(seed.cityEn() + " - inspection site");
        listing.setPriceLabelAr(seed.priceAr());
        listing.setPriceLabelEn(seed.priceEn());
        listing.setMeasureLabel(seed.measureLabel());
        listing.setFeatured(seed.featured());
        listing.setPublishDate(publishDate);
        listing.setExpireDate(publishDate.plusDays(45));
        listing.setAuctionDate(publishDate.plusDays(30));
        listing.setAuctionTime(LocalTime.of(12 + (index % 3), 0));
        listing.setBeneficiaryAr("لصالح إحدى الجهات المالكة");
        listing.setBeneficiaryEn("For an asset-owning organization");
        listing.setVenueAr(seed.cityAr() + " - قاعة المزاد");
        listing.setVenueEn(seed.cityEn() + " - auction venue");
        listing.setAnnouncementSourceAr("إعلان رسمي");
        listing.setAnnouncementSourceEn("Official announcement");
        listing.setNotesAr("تراجع الشروط النهائية والمستندات مع فريق الحبشي قبل جلسة المزاد.");
        listing.setNotesEn("Confirm the final terms and documents with El Habashy before the auction session.");
        listing.setMapUrl("https://maps.google.com/?q=" + seed.cityEn().replace(" ", "%20"));
        listing.setSeoTitleAr(seed.titleAr());
        listing.setSeoTitleEn(seed.titleEn());
        listing.setSeoDescriptionAr(seed.summaryAr());
        listing.setSeoDescriptionEn(seed.summaryEn());
        listing.setSeoKeywordsAr("مزاد، " + seed.cityAr() + "، " + sector.getTitleAr());
        listing.setSeoKeywordsEn("auction, " + seed.cityEn() + ", " + sector.getTitleEn());
        listing.setViews(350L + (index * 173L));
        listing.setWhatsappClicks(8L + (index * 5L));
        listing.setCreatedAt(Instant.parse("2026-06-01T09:00:00Z").plusSeconds(index * 86400L));
        listing.replaceSeedImages(imagesFor(seed.sectorCode()));
        listing.replaceSpecifications(specificationsFor(seed, sector));
        return listing;
    }

    private List<String> imagesFor(String sectorCode) {
        return switch (sectorCode) {
            case "real-estate" -> List.of(
                    "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=84");
            case "cars" -> List.of(
                    "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1200&q=84");
            case "antiques" -> List.of(
                    "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1603204077779-bed963ea7d0e?auto=format&fit=crop&w=1200&q=84");
            case "scrap" -> List.of(
                    "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1581093458791-9d15482778a1?auto=format&fit=crop&w=1200&q=84");
            case "movables" -> List.of(
                    "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1565793298595-6a879b1d9492?auto=format&fit=crop&w=1200&q=84");
            default -> List.of(
                    "https://images.unsplash.com/photo-1581092160562-40aa08e78837?auto=format&fit=crop&w=1600&q=84",
                    "https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=1200&q=84");
        };
    }

    private List<ListingSpecification> specificationsFor(ListingSeed seed, Sector sector) {
        return List.of(
                specification("القطاع", "Category", sector.getTitleAr(), sector.getTitleEn()),
                specification("المدينة", "City", seed.cityAr(), seed.cityEn()),
                specification("الكمية / المساحة", "Quantity / area", seed.measureLabel(), seed.measureLabel())
        );
    }

    private ListingSpecification specification(String labelAr, String labelEn, String valueAr, String valueEn) {
        ListingSpecification specification = new ListingSpecification();
        specification.setLabelAr(labelAr);
        specification.setLabelEn(labelEn);
        specification.setValueAr(valueAr);
        specification.setValueEn(valueEn);
        return specification;
    }

    private record ListingSeed(
            String slug,
            String titleAr,
            String titleEn,
            String summaryAr,
            String summaryEn,
            String sectorCode,
            ListingStatus status,
            String cityAr,
            String cityEn,
            String priceAr,
            String priceEn,
            String measureLabel,
            boolean featured
    ) {
    }
}
