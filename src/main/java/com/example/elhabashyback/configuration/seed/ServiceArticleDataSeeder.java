package com.example.elhabashyback.configuration.seed;

import com.example.elhabashyback.expertise.entity.ServiceArticle;
import com.example.elhabashyback.expertise.entity.ServiceKind;
import com.example.elhabashyback.expertise.repository.ServiceArticleRepository;
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
public class ServiceArticleDataSeeder implements ApplicationRunner {

    private static final List<ServiceSeed> ARTICLES = List.of(
            new ServiceSeed(
                    "valuation-reports-feasibility-studies",
                    ServiceKind.VALUATION,
                    "قسم التقييمات والتقارير ودراسات الجدوى",
                    "Valuation, Reports & Feasibility Studies",
                    "خبراء عقاريون واستشاريون ومحللون اقتصاديون لتقييم الأصول والمشروعات بأحدث المناهج العالمية.",
                    "Specialists in asset valuation, reporting and real-estate feasibility studies.",
                    "<h2>قسم التقييمات والتقارير ودراسات الجدوى</h2><p>يضم القسم نخبة من الخبراء العقاريين والاستشاريين والمحللين الاقتصاديين لتقييم الأراضي والأصول العقارية والمشروعات والمنشآت الصناعية والزراعية وفق مناهج التقييم المعترف بها.</p><h3>يتخصص القسم في:</h3><ul><li>تقييم الأراضي والمراكز التجارية.</li><li>تقييم المشروعات الصناعية والزراعية.</li><li>تقييم المعدات والآلات والسيارات.</li><li>إعداد دراسات الجدوى العقارية.</li><li>تقييم التحف والأنتيكات.</li></ul>",
                    "<h2>Valuation, Reports & Feasibility Studies</h2><p>Our experts value land, property assets, industrial and agricultural projects using internationally recognized methodologies.</p><ul><li>Land and commercial centres</li><li>Industrial and agricultural projects</li><li>Equipment, machinery and vehicles</li><li>Real-estate feasibility studies</li><li>Antiques and collectibles</li></ul>",
                    "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=1600&q=85",
                    List.of(
                            "https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1200&q=85",
                            "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1200&q=85"
                    ),
                    true,
                    0
            ),
            new ServiceSeed(
                    "real-estate-arbitration",
                    ServiceKind.ARBITRATION,
                    "القطاع العقاري",
                    "Real Estate Arbitration",
                    "خبرة فنية وقانونية في المنازعات والتقديرات المرتبطة بالأصول العقارية.",
                    "Technical expertise for real-estate disputes and asset assessments.",
                    "<h2>القطاع العقاري</h2><p>خدمات التحكيم والخبرة الفنية في المنازعات العقارية، وفحص المستندات والتقييمات وإعداد الرأي الفني المتخصص.</p>",
                    "<h2>Real Estate Arbitration</h2><p>Technical review, document examination and specialist opinions for property disputes.</p>",
                    "https://images.unsplash.com/photo-1505664194779-8beaceb93744?auto=format&fit=crop&w=1600&q=85",
                    List.of(
                            "https://images.unsplash.com/photo-1560518883-ce09059eeffa?auto=format&fit=crop&w=1200&q=85",
                            "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1200&q=85"
                    ),
                    true,
                    1
            ),
            new ServiceSeed(
                    "technical-economic-consulting",
                    ServiceKind.CONSULTING,
                    "الاستشارات الفنية والاقتصادية",
                    "Technical & Economic Consulting",
                    "استشارات موثوقة لاتخاذ قرارات استثمارية مبنية على البيانات والخبرة.",
                    "Evidence-led advice for sound investment decisions.",
                    "<h2>الاستشارات</h2><p>نقدم الرأي الفني والاقتصادي ودعم اتخاذ القرار للمؤسسات والمستثمرين في مختلف قطاعات الأصول.</p>",
                    "<h2>Consulting</h2><p>Technical and economic advice for institutions and investors across asset sectors.</p>",
                    "https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&w=1600&q=85",
                    List.of(
                            "https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=1200&q=85",
                            "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?auto=format&fit=crop&w=1200&q=85"
                    ),
                    true,
                    2
            ),
            new ServiceSeed(
                    "movables-vehicles-arbitration",
                    ServiceKind.ARBITRATION,
                    "المنقولات والسيارات",
                    "Movables & Vehicles",
                    "الخبرة والتحكيم في تقييم المنقولات والمعدات والمركبات.",
                    "Arbitration expertise for movables, equipment and vehicles.",
                    "<h2>المنقولات والسيارات</h2><p>فحص وتقييم المنقولات والمعدات والآلات والسيارات وإعداد التقارير الفنية المتخصصة.</p>",
                    "<h2>Movables & Vehicles</h2><p>Technical inspection, valuation and specialist reports.</p>",
                    "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1600&q=85",
                    List.of("https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=85"),
                    false,
                    3
            ),
            new ServiceSeed(
                    "antiques-collectibles-arbitration",
                    ServiceKind.ARBITRATION,
                    "التحف والأنتيكات",
                    "Antiques & Collectibles",
                    "تقييم وفحص التحف والمقتنيات والأنتيكات بمعرفة خبراء متخصصين.",
                    "Specialist appraisal of antiques and collectibles.",
                    "<h2>التحف والأنتيكات</h2><p>الخبرة الفنية والتقييم والتحكيم في التحف والمقتنيات النادرة والأنتيكات.</p>",
                    "<h2>Antiques & Collectibles</h2><p>Specialist appraisal and arbitration for antiques and rare collectibles.</p>",
                    "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=1600&q=85",
                    List.of("https://images.unsplash.com/photo-1603204077779-bed963ea7d0e?auto=format&fit=crop&w=1200&q=85"),
                    false,
                    4
            ),
            new ServiceSeed(
                    "arabian-horses-arbitration",
                    ServiceKind.ARBITRATION,
                    "الخيول العربية",
                    "Arabian Horses",
                    "خبرة متخصصة في تقييم الخيول العربية والأصول المرتبطة بها.",
                    "Specialist expertise in Arabian horse valuation.",
                    "<h2>الخيول العربية</h2><p>تقييم وفحص الخيول العربية وإعداد الرأي الفني والتقارير المتخصصة.</p>",
                    "<h2>Arabian Horses</h2><p>Valuation, inspection and specialist reporting.</p>",
                    "https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?auto=format&fit=crop&w=1600&q=85",
                    List.of("https://images.unsplash.com/photo-1534773728080-33d31da27ae5?auto=format&fit=crop&w=1200&q=85"),
                    false,
                    5
            )
    );

    private final ServiceArticleRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (ServiceSeed seed : ARTICLES) {
            if (!repository.existsBySlugIgnoreCase(seed.slug())) {
                repository.save(toEntity(seed));
            }
        }
    }

    private ServiceArticle toEntity(ServiceSeed seed) {
        ServiceArticle article = new ServiceArticle();
        article.setSlug(seed.slug());
        article.setKind(seed.kind());
        article.setTitleAr(seed.titleAr());
        article.setTitleEn(seed.titleEn());
        article.setSummaryAr(seed.summaryAr());
        article.setSummaryEn(seed.summaryEn());
        article.setContentAr(seed.contentAr());
        article.setContentEn(seed.contentEn());
        article.setHeroImageUrl(seed.image());
        article.setFeatured(seed.featured());
        article.setDisplayOrder(seed.displayOrder());
        article.setSeoTitleAr(seed.titleAr());
        article.setSeoTitleEn(seed.titleEn());
        article.setSeoDescriptionAr(seed.summaryAr());
        article.setSeoDescriptionEn(seed.summaryEn());
        article.setSeoKeywordsAr("التحكيم، التقييم، الاستشارات، " + seed.titleAr());
        article.setSeoKeywordsEn("arbitration, valuation, consulting, " + seed.titleEn());
        article.replaceGallery(seed.gallery());
        return article;
    }

    private record ServiceSeed(
            String slug,
            ServiceKind kind,
            String titleAr,
            String titleEn,
            String summaryAr,
            String summaryEn,
            String contentAr,
            String contentEn,
            String image,
            List<String> gallery,
            boolean featured,
            int displayOrder
    ) {
    }
}
