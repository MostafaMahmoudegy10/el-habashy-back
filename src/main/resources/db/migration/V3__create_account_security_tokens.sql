create table email_verification_tokens (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    token_hash varchar(64) not null unique,
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone,
    created_at timestamp with time zone not null
);

create index idx_email_verification_user on email_verification_tokens(user_id);

create table password_reset_otps (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    otp_hash varchar(64) not null,
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone,
    failed_attempts integer not null default 0,
    created_at timestamp with time zone not null
);

create index idx_password_reset_user on password_reset_otps(user_id);
