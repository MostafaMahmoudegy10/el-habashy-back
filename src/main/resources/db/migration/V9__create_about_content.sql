create table about_profile (
    id smallint primary key,
    headline_ar text not null,
    headline_en text not null,
    profile_ar text not null,
    profile_en text not null,
    mission_ar text not null,
    mission_en text not null,
    vision_ar text not null,
    vision_en text not null,
    profile_image_url text,
    started_year integer not null,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_profile_singleton check (id = 1),
    constraint chk_about_profile_started_year check (started_year between 1900 and 2100)
);

create table about_people (
    id bigserial primary key,
    name_ar varchar(255) not null,
    name_en varchar(255) not null,
    role_ar varchar(255) not null,
    role_en varchar(255) not null,
    biography_ar text not null,
    biography_en text not null,
    image_url text,
    display_order integer not null default 0,
    active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_people_order check (display_order >= 0)
);

create index idx_about_people_order on about_people(active, display_order, id);

create table about_departments (
    id bigserial primary key,
    title_ar varchar(255) not null,
    title_en varchar(255) not null,
    description_ar text not null,
    description_en text not null,
    display_order integer not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_departments_order check (display_order >= 0)
);

create index idx_about_departments_order on about_departments(display_order, id);

create table about_certificates (
    id bigserial primary key,
    title_ar varchar(500) not null,
    title_en varchar(500) not null,
    issuer_ar varchar(500) not null,
    issuer_en varchar(500) not null,
    description_ar text not null,
    description_en text not null,
    issue_date date,
    image_url text,
    display_order integer not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_certificates_order check (display_order >= 0)
);

create index idx_about_certificates_order on about_certificates(display_order, id);

create table about_work_categories (
    id bigserial primary key,
    title_ar varchar(255) not null,
    title_en varchar(255) not null,
    summary_ar text not null,
    summary_en text not null,
    display_order integer not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_work_categories_order check (display_order >= 0)
);

create index idx_about_work_categories_order on about_work_categories(display_order, id);

create table about_work_entries (
    id bigserial primary key,
    category_id bigint not null references about_work_categories(id) on delete cascade,
    title_ar varchar(500) not null,
    title_en varchar(500) not null,
    client_ar varchar(500) not null,
    client_en varchar(500) not null,
    summary_ar text not null,
    summary_en text not null,
    details_ar text not null,
    details_en text not null,
    project_year integer,
    location_ar varchar(500) not null,
    location_en varchar(500) not null,
    image_url text,
    display_order integer not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint chk_about_work_entries_order check (display_order >= 0),
    constraint chk_about_work_entries_year check (project_year is null or project_year between 1900 and 2100)
);

create index idx_about_work_entries_category_order on about_work_entries(category_id, display_order, id);

insert into about_profile (
    id, headline_ar, headline_en, profile_ar, profile_en,
    mission_ar, mission_en, vision_ar, vision_en, profile_image_url, started_year
) values (
    1,
    'خبرة ممتدة في التثمين وإدارة المزادات منذ عام 1944',
    'Valuation and auction expertise since 1944',
    'بدأت نواة الشركة من مكتب شيخ الخبراء المثمنين سيد الحبشي، وتطورت لتصبح الحبشي للخبراء المثمنين للخبرة والتثمين، إحدى الجهات المصرية العريقة في تقييم الأصول وإدارة المزادات والخبرة الفنية. تمتد خبراتنا إلى العقارات والمنقولات والسيارات والمعدات والمخلفات والتحف، مع إعداد تقارير مهنية واضحة ودعم الجهات المالكة في كل مرحلة.',
    'The company began with the office of senior valuation expert Sayed El Habashy and evolved into El Habashy Valuation Experts for Expertise and Appraisal, a long-established Egyptian practice in asset valuation, auctions and technical expertise. Our work covers real estate, movables, vehicles, machinery, scrap and antiques, supported by clear professional reporting throughout every engagement.',
    'تقديم تقييمات وخبرات فنية مستقلة، وإدارة إجراءات المزادات بصورة منظمة وشفافة تحفظ قيمة الأصول وتدعم القرار.',
    'To deliver independent valuation and technical expertise, and to manage auctions through transparent, organized processes that protect asset value and support sound decisions.',
    'أن تظل الحبشي مرجعًا موثوقًا للخبرة والتثمين في مصر، تجمع بين الخبرة المتوارثة والأدوات الحديثة وجودة التوثيق.',
    'To remain a trusted Egyptian reference for expertise and appraisal by combining inherited experience, modern tools and rigorous documentation.',
    'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1600&q=85',
    1944
);

insert into about_people (name_ar, name_en, role_ar, role_en, biography_ar, biography_en, display_order) values
('سيد سيد الحبشي', 'Sayed Sayed El Habashy', 'خبير مثمن ورئيس مجلس الإدارة', 'Valuation Expert and Chairman', 'خبرة ممتدة في التثمين والخبرة الفنية وإدارة ملفات الأصول والمزادات.', 'Extensive experience in valuation, technical expertise and the management of asset and auction engagements.', 0),
('سعيد سيد الحبشي', 'Saied Sayed El Habashy', 'خبير مثمن وعضو مجلس الإدارة', 'Valuation Expert and Board Member', 'متخصص في فحص الأصول وإعداد الرأي الفني ومتابعة إجراءات التقييم.', 'Specialized in asset inspection, technical opinions and valuation processes.', 1),
('محمد سيد الحبشي', 'Mohamed Sayed El Habashy', 'خبير مثمن وعضو مجلس الإدارة', 'Valuation Expert and Board Member', 'خبرة عملية في إدارة أعمال المعاينات والتثمين والتنسيق مع الجهات المالكة.', 'Practical experience in inspections, appraisal and coordination with asset owners.', 2),
('مصطفى سيد سيد الحبشي', 'Mostafa Sayed Sayed El Habashy', 'مدير تطوير الأعمال', 'Business Development Director', 'مسؤول عن تطوير الخدمات الرقمية وتنظيم رحلة العميل ورفع كفاءة عرض الأصول.', 'Responsible for digital services, customer journeys and improving asset presentation.', 3),
('أحمد سعيد سيد الحبشي', 'Ahmed Saied Sayed El Habashy', 'خبير تقييم ومتابعة', 'Valuation and Operations Expert', 'يتابع أعمال التقييم والمعاينات وتجهيز البيانات والمستندات الفنية.', 'Supports valuation, inspections and the preparation of technical data and documents.', 4);

insert into about_departments (title_ar, title_en, description_ar, description_en, display_order) values
('قطاع العقارات', 'Real Estate', 'تقييم الأراضي والمباني والوحدات والمشروعات العقارية.', 'Valuation of land, buildings, units and real-estate projects.', 0),
('قطاع المنقولات والمعدات', 'Movables and Equipment', 'فحص وتقييم الآلات وخطوط الإنتاج والمخزون والمنقولات.', 'Inspection and valuation of machinery, production lines, inventory and movables.', 1),
('قطاع السيارات', 'Vehicles', 'تقييم المركبات والأساطيل وتجهيز بيانات المعاينة والمزاد.', 'Vehicle and fleet valuation with inspection and auction documentation.', 2),
('قطاع المخلفات والسكراب', 'Scrap and Leftovers', 'تقييم المخلفات التشغيلية والخردة والكميات الصناعية.', 'Valuation of operational leftovers, scrap and industrial quantities.', 3),
('التحف والمقتنيات', 'Antiques and Collectibles', 'فحص وتقييم التحف والأعمال الفنية والمقتنيات.', 'Inspection and appraisal of antiques, art and collectibles.', 4),
('الشؤون القانونية والتحكيم', 'Legal Affairs and Arbitration', 'دعم الخبرة الفنية وفض المنازعات ومراجعة المستندات.', 'Technical expertise for disputes, arbitration and document review.', 5);

insert into about_certificates (title_ar, title_en, issuer_ar, issuer_en, description_ar, description_en, issue_date, image_url, display_order) values
('تقدير التعاون والخبرة المهنية', 'Professional Expertise Recognition', 'إحدى الجهات المتعاونة', 'Partner Organization', 'تقدير لمسيرة مهنية ممتدة في أعمال الخبرة والتثمين وإدارة المزادات وخدمة المؤسسات.', 'Recognition of a long professional track record in valuation, auctions and institutional services.', '2015-01-01', 'https://images.unsplash.com/photo-1589330694653-ded6df03f754?auto=format&fit=crop&w=1200&q=85', 0),
('شهادة تقدير للأداء المؤسسي', 'Institutional Performance Recognition', 'جهة مؤسسية', 'Institutional Client', 'تقدير للالتزام بالمعايير المهنية والدقة في إعداد تقارير التقييم والخبرة الفنية.', 'Recognition for professional standards and accuracy in technical valuation reports.', '2018-01-01', 'https://images.unsplash.com/photo-1565688527174-775059ac429c?auto=format&fit=crop&w=1200&q=85', 1),
('تكريم سنوات الخبرة والعطاء', 'Years of Experience Recognition', 'ملتقى مهني', 'Professional Forum', 'تكريم للخبرة المتراكمة والمساهمة في تطوير خدمات التثمين والتحكيم والاستشارات.', 'Recognition of accumulated expertise in valuation, arbitration and consulting.', '2022-01-01', 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=85', 2);

insert into about_work_categories (title_ar, title_en, summary_ar, summary_en, display_order) values
('البنوك والمؤسسات المالية', 'Banks and Financial Institutions', 'خبرات في تقييم الأصول والمنقولات وتجهيز ملفات البيع والمزادات لصالح المؤسسات المالية.', 'Experience in asset valuation, movables and auction documentation for financial institutions.', 0),
('الوزارات والهيئات', 'Ministries and Authorities', 'أعمال خبرة وتثمين لجهات حكومية وهيئات عامة في قطاعات متعددة.', 'Valuation and technical expertise for public authorities across multiple sectors.', 1),
('الشركات والصناعات', 'Companies and Industries', 'تقييم خطوط إنتاج ومعدات ومخزون وأصول صناعية وتجارية.', 'Valuation of production lines, machinery, inventory and industrial assets.', 2),
('المحافظات والمشروعات المحلية', 'Governorates and Local Projects', 'معاينات وتقييمات لأصول ومشروعات موزعة في عدد من المحافظات المصرية.', 'Inspections and valuations for assets and projects across Egyptian governorates.', 3);

insert into about_work_entries (
    category_id, title_ar, title_en, client_ar, client_en,
    summary_ar, summary_en, details_ar, details_en,
    project_year, location_ar, location_en, display_order
) values
((select id from about_work_categories where title_en = 'Banks and Financial Institutions'), 'تقييم أصول ومنقولات مصرفية', 'Banking Assets and Movables Valuation', 'البنك الأهلي المصري', 'National Bank of Egypt', 'فحص وتقييم مجموعة متنوعة من الأصول والمنقولات.', 'Inspection and valuation of a varied portfolio of assets and movables.', 'شملت الأعمال مراجعة البيانات المتاحة، والمعاينة الميدانية، وتصنيف الحالة، وإعداد مخرجات فنية واضحة تدعم إجراءات التصرف في الأصول.', 'The engagement included data review, site inspection, condition classification and clear technical outputs supporting asset-disposal decisions.', 2021, 'القاهرة', 'Cairo', 0),
((select id from about_work_categories where title_en = 'Banks and Financial Institutions'), 'إعداد ملفات تقييم لأصول متعددة', 'Multi-asset Valuation Portfolio', 'بنك مصر', 'Banque Misr', 'تنظيم وتقييم أصول ضمن أكثر من موقع وبطبيعة استخدام مختلفة.', 'Organizing and valuing assets across multiple locations and use cases.', 'تم توحيد منهج جمع البيانات وإعداد أوصاف الأصول ومراجعة المستندات وإصدار تقارير قابلة للمراجعة والمتابعة.', 'A consistent workflow was used for data collection, asset descriptions, document review and auditable reporting.', 2022, 'عدة محافظات', 'Multiple governorates', 1),
((select id from about_work_categories where title_en = 'Ministries and Authorities'), 'خبرة فنية لأصول تابعة لجهة حكومية', 'Technical Expertise for Public Assets', 'وزارة الإسكان والمجتمعات العمرانية', 'Ministry of Housing and Urban Communities', 'معاينات ودراسة بيانات أصول عقارية ومنقولات مرتبطة بها.', 'Inspection and study of real-estate assets and related movables.', 'تضمنت المهمة حصر العناصر محل الدراسة، وتوثيق حالتها، وتحليل البيانات المتاحة، وتقديم رأي فني منظم.', 'The work covered asset inventory, condition documentation, available-data analysis and a structured technical opinion.', 2020, 'القاهرة الكبرى', 'Greater Cairo', 0),
((select id from about_work_categories where title_en = 'Ministries and Authorities'), 'تقييم أصول تشغيلية ومعدات', 'Operational Assets and Equipment Valuation', 'الهيئة القومية للإنتاج الحربي', 'National Organization for Military Production', 'تقييم معدات وأصول تشغيلية وفقًا للمعاينة والبيانات الفنية.', 'Valuation of equipment and operational assets based on inspection and technical data.', 'ركزت الأعمال على توصيف الأصل وحالته التشغيلية وعمره الإنتاجي والعوامل المؤثرة في القيمة.', 'The engagement focused on asset description, operating condition, productive age and key value drivers.', 2019, 'القاهرة', 'Cairo', 1),
((select id from about_work_categories where title_en = 'Companies and Industries'), 'تقييم معدات وخطوط إنتاج', 'Machinery and Production-line Valuation', 'النصر لصناعة السيارات', 'El Nasr Automotive Manufacturing', 'فحص معدات وخطوط تشغيل وأصول صناعية داخل مواقع الإنتاج.', 'Inspection of machinery, operating lines and industrial assets at production sites.', 'شملت المهمة توثيق المكونات والحالة الفنية وتحديد عوامل الاستهلاك وإعداد بيانات تفصيلية لكل مجموعة أصول.', 'The work documented components and technical condition, identified depreciation factors and prepared detailed records for each asset group.', 2018, 'حلوان', 'Helwan', 0),
((select id from about_work_categories where title_en = 'Companies and Industries'), 'تقييم أصول صناعية ومخزون', 'Industrial Assets and Inventory Valuation', 'مصر للألومنيوم', 'Egyptalum', 'تقييم مجموعات من المعدات والمخزون والأصول المساندة للتشغيل.', 'Valuation of machinery, inventory and supporting operational assets.', 'تم تنظيم الأصول في مجموعات قابلة للمراجعة وربط نتائج المعاينة بالبيانات والمستندات الفنية المتاحة.', 'Assets were organized into reviewable groups, linking inspection results with available technical data and documents.', 2023, 'نجع حمادي', 'Nag Hammadi', 1),
((select id from about_work_categories where title_en = 'Governorates and Local Projects'), 'معاينات وتقييم أصول متنوعة', 'Mixed-asset Inspection and Valuation', 'جهات وأعمال بمحافظة القاهرة', 'Cairo-based Organizations', 'خبرات سابقة في تقييم عقارات ومنقولات وسيارات داخل نطاق القاهرة.', 'Previous work valuing property, movables and vehicles across Cairo.', 'تم تنفيذ المعاينات وفق خطط زمنية واضحة مع توثيق المواقع والحالات وإعداد مخرجات مناسبة لطبيعة كل أصل.', 'Inspections followed clear schedules with documented locations and conditions, producing outputs tailored to each asset type.', 2024, 'القاهرة', 'Cairo', 0),
((select id from about_work_categories where title_en = 'Governorates and Local Projects'), 'تقييمات متعددة القطاعات', 'Multi-sector Valuation Assignments', 'جهات وأعمال بمحافظة الإسكندرية', 'Alexandria-based Organizations', 'أعمال تقييم لعقارات ومنقولات وأصول تجارية وصناعية.', 'Valuation assignments for real estate, movables and commercial and industrial assets.', 'شملت الأعمال التنسيق للمعاينة، وتجميع المستندات، وتصنيف الأصول، وإعداد تقارير تدعم القرار.', 'The work included inspection coordination, document collection, asset classification and decision-support reporting.', 2022, 'الإسكندرية', 'Alexandria', 1);
