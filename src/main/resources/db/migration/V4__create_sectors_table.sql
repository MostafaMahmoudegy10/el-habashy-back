create table sectors (
    code varchar(50) primary key,
    display_order integer not null unique,
    title_ar varchar(255) not null,
    title_en varchar(255) not null,
    description_ar text not null,
    description_en text not null,
    updated_at timestamp with time zone not null,
    constraint chk_sectors_display_order_non_negative check (display_order >= 0)
);
