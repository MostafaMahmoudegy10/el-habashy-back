alter table listing_media add column uploaded_bytes bigint not null default 0;

alter table listing_media
    add constraint chk_listing_media_uploaded_bytes check (uploaded_bytes >= 0);

create unique index uk_listing_media_single_thumbnail
    on listing_media(listing_id)
    where media_role = 'THUMBNAIL' and upload_status <> 'FAILED';

create unique index uk_listing_media_single_video
    on listing_media(listing_id)
    where media_role = 'VIDEO' and upload_status <> 'FAILED';
