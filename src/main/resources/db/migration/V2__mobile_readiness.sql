-- Phase A: make the API consumable by a mobile client.
--   1. Long-form text columns (the DTO validation already allowed more than 255 chars).
--   2. Per-device JWT sessions, so a phone and the admin web front can stay logged in
--      at the same time.
--   3. Indexes on the token columns, which JwtFilter hits on every single request.

-- 1. Long-form text -----------------------------------------------------------
-- QuoteRequest validates content up to 500 chars but the column was varchar(255):
-- any longer quote passed validation and then failed at insert time.
alter table quotes alter column content type text;
alter table authors alter column bio type text;
alter table themes alter column description type text;

-- 2. Per-device sessions ------------------------------------------------------
alter table jwts add column device_id varchar(100);
alter table jwts add column device_name varchar(120);
alter table jwts add column platform varchar(20);

-- Existing rows belong to no identified device; they stay valid until they expire.
create index idx_jwt_user_device on jwts (user_id, device_id);

-- 3. Hot lookup paths ---------------------------------------------------------
create index idx_jwt_expire on jwts (expire);
create index idx_refresh_token_token on refresh_tokens (token);
create index idx_jwt_token on jwts (token);
