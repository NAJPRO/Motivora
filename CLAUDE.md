# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start infrastructure (PostgreSQL on :5432, pgAdmin on :8081, smtp4dev on :8082/:9025)
docker compose up -d

# Run the application
./mvnw spring-boot:run

# Build (skip tests)
./mvnw clean package -DskipTests

# Unit tests (integration tests are excluded by default)
./mvnw test

# Integration tests — needs a Docker daemon (Testcontainers starts PostgreSQL)
./mvnw verify -Pintegration-tests

# Run a single test class / method
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName
```

The API is served at `http://localhost:8080/api/v1`. Swagger UI is at
`http://localhost:8080/api/swagger-ui.html`.

## Architecture

Standard layered Spring Boot architecture: `controller → service/Impl → repository → entity`.

### Package structure

```
config/       ← ApiVersionConfig (path prefix), LegacyApiVersionFilter, JwtProperties, WebMvcConfig
controller/
  Auth/       ← register, login, refresh-token, sessions, reset-password, verify-email
  Admin/      ← CRUD for quotes, authors, themes, users, stats, image uploads (ADMIN only)
  Client/     ← me, favorites, devices, notifications, public quote/theme/author browsing
service/
  Impl/       ← concrete implementations
  mails/      ← legacy email helpers (WelcomeMail) — prefer the notification/ subsystem
notification/
  api/        ← NotificationService facade (templated email)
  service/    ← TemplateService (renders Thymeleaf templates)
  transporteur/ ← NotificationTransporter implementations (EmailTransporter, ...)
  push/       ← PushSender abstraction: ExpoPushSender, LoggingPushSender
  model/      ← NotificationTemplateType enum
scheduler/    ← DailyQuoteScheduler + DailyQuotePicker
storage/      ← FileStorageService (image uploads), LocalFileStorageService
mapper/
  Impl/       ← manual DTO↔entity conversions (no MapStruct)
dto/
  Annotation/ ← custom validation annotations (@UniqueField, @ExistField)
  Validation/ ← their ConstraintValidator implementations
  response/common/ ← MessageResponse, PageResponse (the shared envelopes)
entity/
  listener/   ← JPA lifecycle listeners for slug generation
security/
  device/     ← DeviceContext + resolver (per-device sessions)
  ratelimit/  ← RateLimitStore abstraction + in-memory implementation
exception/    ← CustomExceptionHandler (@RestControllerAdvice) + BusinessException
utils/
  seeders/    ← RoleSeeder, AdminSeeder (CommandLineRunner, run at startup)
```

### Key design patterns

**API versioning** — `ApiVersionConfig` prefixes every controller with `/v1`, so nothing is
served unversioned and no controller can forget it. `LegacyApiVersionFilter` rewrites
unversioned URLs (`/api/quotes` → `/api/v1/quotes`) on a request wrapper, so the security
chain and the MVC mapping see the same path. That filter is transitional: drop it once the
admin front calls versioned URLs.

**`idOrSlug` routing** — All service `find*` methods accept either an integer ID or a string slug. The check is `idOrSlug.matches("\\d+")`. Controllers expose `@PathVariable String idOrSlug` accordingly.

**Slug auto-generation** — Entity listeners in `entity/listener/` (`QuoteSlug`, `AuthorSlug`, `ThemeSlug`) use `@PrePersist` / `@PreUpdate` to call `Slug.toSlug()`, which lowercases and replaces non-alphanumeric characters with hyphens.

**Manual mappers** — No MapStruct. Each mapper interface has a concrete `Impl/` class. Pattern: `toEntity(Request)`, `toResponse(Entity)`, `toEntityUpdate(Entity, Request)`.

**Public vs admin quote shape** — `QuoteMapper.toResponse()` is the public shape and omits
`createdByUser`; `toAdminResponse()` includes it. The public catalogue is reachable
anonymously, and `UserDTOResponse` carries an email address — never return it there.

**Per-device JWT sessions** — `JwtService` persists every issued token as a `Jwt` (with an
embedded `RefreshToken`) carrying the `deviceId`/`platform` from the `X-Device-Id` /
`X-Client-Platform` headers (`security/device/`). A new login revokes only the sessions of
that *same* device, so a phone and the admin web front stay signed in at once.
`disableTokens(user)` revokes everything and is used on privilege, status or password
changes. A `@Scheduled` task purges revoked/expired rows every 5 minutes.

**Token transport and rotation** — The access token is a `Bearer` token in the
`Authorization` header. The refresh token is returned **in the JSON body** (mobile clients
have no usable cookie jar) *and* in the `refreshToken` HttpOnly cookie (web). Refreshing
rotates the token: the presented one is revoked, and replaying it revokes every session of
that user. Token failures throw `SecurityException` → **401**, never 400, so a client
interceptor can trigger its refresh flow. TTLs come from `app.jwt.*` (access 30 min,
refresh 60 days).

**Response envelopes** — Acknowledgement endpoints return `MessageResponse`; paginated
endpoints return `PageResponse<T>` (`items`, `page`, `size`, `totalElements`, `totalPages`,
`hasNext`, `hasPrevious`) rather than Spring's `Page`, whose serialized shape is unstable
between versions. Errors always use `ApiError` — `code`, a stable machine `error`, a
human `message`, and `fieldErrors` on validation failures.

**Notification subsystem** — Two channels, deliberately separate.
*Email*: `NotificationServiceImpl.sendNotification(target, NotificationTemplateType, data)`
runs `@Async`, resolves the template from the enum (`WELCOME` → `mails/welcome-email`),
renders it through Thymeleaf and fans it out to every `NotificationTransporter` that
supports the type.
*Push*: `PushSender` delivers a `PushMessage` to device tokens. `app.push.provider=expo`
uses Expo's service (covers APNs + FCM); `none` (default) logs instead, so the pipeline is
exercisable without credentials. `DailyQuoteScheduler` runs hourly and sends to the users
whose *local* hour matches their preference, guarded by `lastSentOn`.

**DB-backed bean validation** — `@UniqueField(entity=X.class, fieldName="f")` and `@ExistField(...)` are field-level constraints whose validators run a dynamic JPQL `COUNT` via `EntityManager` (unique → count 0, exist → count 1). Used on request DTOs to enforce uniqueness/existence at validation time instead of in the service layer.

**Role seeding** — `RoleSeeder` (CommandLineRunner) inserts all `UserRole` enum values (`USER`, `ADMIN`, `MODERATOR`, `CREATOR`) into the database on startup if they don't exist yet. `AdminSeeder` optionally bootstraps an ADMIN from `MOTIVORA_ADMIN_EMAIL` / `MOTIVORA_ADMIN_PASSWORD`. The database must be running before starting the app.

**Schema migrations** — Flyway owns the schema and `ddl-auto: validate` checks the entities
against it, so every entity change needs a matching `V<n>__*.sql`. V1 baseline, V2 mobile
readiness (per-device sessions, TEXT columns), V3 account management (avatars, purpose-scoped
OTPs), V4 push notifications.

**Exception handling** — Throw `BusinessException(errorCode, message)` for domain errors
(→ 400 with that code). `EntityNotFoundException` → 404, `SecurityException` → 401,
`AccessDeniedException` → 403, `IllegalStateException` → 409, validation → 400 + `fieldErrors`.

### Security

Matchers are written against the versioned path (`/v1/...`) relative to the `/api` context
path. All routes require authentication except (`ConfigSecurityApplication`):
- any `OPTIONS /**` (CORS preflight)
- `POST /v1/auth/login`, `/v1/auth/register`, `/v1/auth/refresh-token` — the rest of
  `/auth` (logout, sessions) acts on the caller's own session and needs a token
- `POST /v1/reset-password/**`, `POST /v1/verify-email/**`
- `GET /v1/quotes/**`, `/v1/themes/**`, `/v1/authors/**` (public catalogue)
- `GET /uploads/**` (stored images), `/actuator/health`, `/actuator/info`, the OpenAPI paths

Session policy is `STATELESS`; `JwtFilter` runs before `UsernamePasswordAuthenticationFilter`
and rejects a bad token with a JSON 401. `RateLimitFilter` throttles login, registration,
password reset and email verification per IP — it reads `getRemoteAddr()` and deliberately
*not* `X-Forwarded-For`, which a client can forge; behind a proxy, set
`server.forward-headers-strategy`. The JWT secret comes from `MOTIVORA_JWT_SECRET`.

### Infrastructure services

| Service  | URL                    | Credentials         |
|----------|------------------------|---------------------|
| pgAdmin  | http://localhost:8081  | admin@motivora.dev / admin |
| smtp4dev | http://localhost:8082  | (no auth, catches all outbound mail) |
