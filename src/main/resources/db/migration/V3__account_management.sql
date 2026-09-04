-- Phase B: self-service account management and email verification.
--   1. A user can carry an avatar and record when their account was deleted.
--   2. An OTP now has a purpose, so email verification and password reset can be
--      pending at the same time — hence the drop of the one-code-per-user constraint.

-- 1. User profile -------------------------------------------------------------
alter table users add column avatar_url varchar(500);
alter table users add column deleted_at timestamp(6);

-- 2. Purpose-scoped OTP codes -------------------------------------------------
alter table otp_codes add column purpose varchar(30);
update otp_codes set purpose = 'PASSWORD_RESET' where purpose is null;
alter table otp_codes alter column purpose set not null;

-- The @OneToOne mapping had made user_id unique, which allowed a single pending code.
do $$
declare
    constraint_name text;
begin
    select conname into constraint_name
    from pg_constraint
    where conrelid = 'otp_codes'::regclass
      and contype = 'u'
      and conkey = array[(select attnum from pg_attribute
                          where attrelid = 'otp_codes'::regclass and attname = 'user_id')];
    if constraint_name is not null then
        execute format('alter table otp_codes drop constraint %I', constraint_name);
    end if;
end $$;

create index idx_otp_user_purpose on otp_codes (user_id, purpose);
