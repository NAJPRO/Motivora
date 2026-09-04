# Motivora Backend — Production-Readiness Audit

**Date:** 2026-06-07
**Scope:** full `src/main` + `src/test`, build/config, Docker, security.

**Verdict up front:** the architecture *skeleton* is reasonable (clean layering, a nicely-designed notification strategy pattern), but the application is **not production-ready and is not currently functional in several core flows**. There are multiple Critical defects that will crash or silently break at runtime, plus serious security exposure. Below, every issue has Problem → Impact → Solution → Priority.

---

## 🔴 CRITICAL BLOCKERS (app is broken / exposed today)

### C1 — Infinite recursion in DTO mapping → `StackOverflowError`
**Problem:** `ThemeMapperImpl.toResponse(theme)` maps `theme.getQuotes()` through `QuoteMapperImpl.toResponse`, which maps `quote.getTheme()` back through `ThemeMapperImpl.toResponse`, which again maps that theme's quotes… (`QuoteMapperImpl.java:35` ↔ `ThemeMapperImpl.java:38`).
**Impact:** Any endpoint returning a `Quote` or `Theme` crashes: `GET /quotes`, `/quotes/{id}`, `/quotes/search`, `/quotes/random`, `/quotes/author/{id}`, `GET /admin/themes`. That's essentially the entire read API.
**Solution:** Break the cycle — `QuoteResponse` should carry a lightweight theme (id/name/slug, no quotes), and `ThemeResponse` should not embed full quotes by default (or expose a separate paginated `/themes/{id}/quotes`). Stop mappers from recursing into back-references.
**Priority:** Critical.

### C2 — Quote creation cannot work (NOT NULL violations + dropped fields)
**Problem:** `QuoteMapperImpl.toEntity` (`:19`) sets only `content`. It never sets `theme` (`Quote.theme` is `nullable=false`), `author`, or `createdByUser`. Worse, `QuoteRequest` has **no `themeId` field at all** (only `content`, `authorId`, `createdByUserId`), and the validated `authorId`/`createdByUserId` are silently ignored by the mapper.
**Impact:** `POST /admin/quotes` always fails (null `theme_id`, and see C3 null `slug`). The core admin capability is non-functional.
**Solution:** Add `themeId` to `QuoteRequest`; resolve `Author`/`Theme`/`User` by id in the service and set them on the entity; derive `createdByUser` from the authenticated principal rather than trusting the client.
**Priority:** Critical.

### C3 — Wrong `@EntityListeners` on `Quote` → slug never generated
**Problem:** `Quote` declares `@EntityListeners(QuoteStatus.class)` (`Quote.java:32`) — `QuoteStatus` is an **enum**, not a listener. The real `QuoteSlug` listener exists but is never wired. `Quote.slug` is `nullable=false`.
**Impact:** Even after C2 is fixed, inserts fail because `slug` is null. Quote slugs are never produced.
**Solution:** `@EntityListeners(QuoteSlug.class)`.
**Priority:** Critical.

### C4 — Spring Data REST auto-exposes every repository (including User/Jwt/OtpCode)
**Problem:** `spring-boot-starter-data-rest` is on the classpath and **every** repository is annotated `@RepositoryRestResource` — `UserRepository`, `JwtRepository`, `OtpCodeRepository`, `RoleRepository`, plus Quote/Author/Theme/Favorite. Spring Data REST generates full CRUD HTTP endpoints for all of them, bypassing your controllers, services, mappers, and validation.
**Impact:** Catastrophic. Any authenticated principal can hit auto-generated endpoints to read password hashes (`User`), JWT/refresh tokens (`Jwt`), password-reset OTP codes (`OtpCode`), and to create/patch/delete records directly. This defeats the entire security/service layer.
**Solution:** Remove the Data REST starter (you don't appear to use it intentionally), or, if kept, set `spring.data.rest.detection-strategy=annotated` and remove `@RepositoryRestResource` from all repos, and explicitly secure `/api` Data REST base path. Recommended: **delete the dependency**.
**Priority:** Critical (security).

### C5 — RBAC defined but never enforced
**Problem:** Roles are seeded and authorities are built (`User.getAuthorities`), but there is no `@EnableMethodSecurity`, no `@PreAuthorize`, and the filter chain only uses `.anyRequest().authenticated()`. `/admin/**` has no role restriction.
**Impact:** Any logged-in `USER` can create/update/disable quotes, authors, and themes. There is effectively no authorization, only authentication.
**Solution:** Add `.requestMatchers("/admin/**").hasRole("ADMIN")` (and appropriate rules), or `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` on admin controllers.
**Priority:** Critical (security).

### C6 — "Public" endpoints require authentication
**Problem:** `PublicQuoteController` is at `/quotes/**` and `FavoriteController` at `/favorites`, but the security config only permits `/auth/**`, `/reset-password/**`, `/test/**`, `/actuator`, and docs. Everything else needs a token.
**Impact:** The documented public quote browsing/search/random feature is locked behind login. Direct contradiction with the README and intended product.
**Solution:** Permit `GET /quotes/**` explicitly (and decide whether favorites are user-scoped/private). Align the matcher list with the actual product intent.
**Priority:** Critical (functional).

### C7 — `getAllFavorites` returns *all users'* favorites; toggle deletes *everyone's*
**Problem:** `FavoriteServiceImpl.getAllFavorites()` does `favoriteRepository.findAll()` with no user scoping. `toogleFavoriteQuote` removes via `favoriteRepository.deleteByQuoteId(quoteId)` — deleting that quote's favorite row for **all** users, not the current one. `Favorite.quote` is `@OneToOne`, which structurally forbids two users favoriting the same quote.
**Impact:** Cross-user data leak (you see everyone's favorites) and data corruption (un-favoriting wipes other users' favorites). Favorites are fundamentally broken.
**Solution:** Scope all favorite queries to `currentUser.getId()`; change `Favorite.quote` to `@ManyToOne`; add unique constraint `(user_id, quote_id)`; delete by `(quoteId, userId)`.
**Priority:** Critical (security + functional).

---

## 🟠 SECURITY WEAKNESSES

### S1 — Hardcoded JWT secret in source (`JwtService.java:41`)
**Impact:** Secret is committed to git; anyone with repo access can forge tokens.
**Solution:** Externalize to env var / secret manager via `@Value`; fail fast if absent.
**Priority:** High (Critical for prod).

### S2 — Raw JWT logged to stdout (`JwtFilter.java:68`) and `security: DEBUG` logging in `application.yml`
**Impact:** Access tokens and security internals land in logs.
**Solution:** Remove `System.out`/`println` debug statements; set security logging to `INFO`/`WARN` in prod; never log tokens.
**Priority:** High.

### S3 — Wrong-password login returns 500, not 401
**Problem:** `authenticationManager.authenticate` throws `BadCredentialsException` (an `AuthenticationException`) inside the controller. The entry point only handles unauthenticated *entry*, so it falls through to the catch-all `Exception` handler → 500.
**Impact:** Auth errors are misreported; clients can't distinguish bad credentials.
**Solution:** Add an `@ExceptionHandler(AuthenticationException.class)` → 401, or wrap login.
**Priority:** High.

### S4 — Tampered/malformed JWT → unhandled exception in filter
**Problem:** `getUserName`/`isTokenExpire` parse+verify with no try/catch in `JwtFilter`. A bad signature throws `JwtException`; filter-thrown exceptions aren't caught by `@RestControllerAdvice`.
**Impact:** 500 (or stack trace) instead of 401.
**Solution:** Wrap parsing in try/catch → 401 via entry point.
**Priority:** High.

### S5 — Password-reset flow incomplete & insecure
**Problem:** `validResetPassword` is an empty stub; there's no controller endpoint to submit a new password; `saveOtpCode` never sets `createdAt`; expiry is never checked on validation; OTPs aren't single-use and accumulate.
**Impact:** Reset is non-functional and, if completed naïvely, replayable.
**Solution:** Implement validation (match + not expired + not consumed), set `confirmedAt`, invalidate prior OTPs, add the `confirm` endpoint, rate-limit requests.
**Priority:** High.

### S6 — Cookie/CORS posture unsafe for prod
**Problem:** Refresh cookie `secure(false)` + `sameSite=None` (`AuthController:120,123`); CORS hardcoded to `http://localhost:3000` with `allowCredentials=true`.
**Impact:** Token cookie transmittable over HTTP; origin not environment-driven.
**Solution:** `secure(true)` in prod, `SameSite=Strict/Lax`, externalize allowed origins per environment.
**Priority:** Medium/High.

### S7 — Actuator fully permitted unauthenticated (`/actuator/**` permitAll)
**Impact:** If exposure is broadened, sensitive endpoints become public.
**Solution:** Restrict to `health`/`info`, secure the rest behind ADMIN.
**Priority:** Medium.

### S8 — Client controls `createdByUserId` on quotes (`QuoteRequest`)
**Impact:** Attribution can be spoofed (and trusting client identity is an anti-pattern).
**Solution:** Derive creator from `AuthUtil.getCurrentUser()`.
**Priority:** Medium.

---

## 🟡 VALIDATION GAPS

- **V1 (High):** `FavoriteController.index(@RequestParam String param)` requires a mandatory unused param (every call 400s without it); `create(@RequestParam Integer id)` on path `/{id}` uses `@RequestParam` instead of `@PathVariable` → the by-id endpoint is broken. `FavoriteRequest` has no validation.
- **V2 (Medium):** `ResetPasswordController.sendOtpCode` omits `@Valid`, so `ResetPasswordDTORequest` constraints never fire.
- **V3 (Medium):** `RegisterDTORequest` names restricted to `^[a-zA-Z0-9_]+$` — rejects spaces and accented characters, i.e. most real names; `first_name`/`last_name` validation messages all say "Pseudo".
- **V4 (Medium):** `QuoteRequest` validates `authorId`/`createdByUserId` existence but the mapper ignores them, and there's no `themeId` to validate (ties to C2).
- **V5 (Low):** No global pagination bounds — `size` is unbounded on `/quotes` (client can request huge pages).

---

## ⚡ PERFORMANCE & N+1

- **P1 (High):** N+1 across the board. `@ManyToOne` (author, theme, createdByUser) is EAGER by default; listing quotes triggers per-row loads, and the theme→quotes mapping (C1) explodes fetch volume. **Fix:** `@EntityGraph`/`join fetch` for read paths, project directly into DTOs, make associations LAZY.
- **P2 (High):** No Flyway/Liquibase; `ddl-auto: update`. **Impact:** No reviewable, versioned schema; risky in prod. **Fix:** Introduce Flyway, switch to `validate`. (Your own Spring standards list Flyway.)
- **P3 (Medium):** `getRandomQuote` loads **all** published quotes into memory then `findAny()` — and `findAny()` isn't random (returns first). **Fix:** `ORDER BY RANDOM() LIMIT 1` (or count + offset).
- **P4 (Medium):** `open-in-view: true` holds DB connections through view rendering and masks lazy-loading bugs. **Fix:** disable and fix fetch strategies.
- **P5 (Medium):** `@Async` on `NotificationServiceImpl.sendNotification` does nothing — there's no `@EnableAsync`. Emails (welcome, OTP) are sent **synchronously** on the request thread, so `register` blocks on SMTP. **Fix:** add `@EnableAsync` (+ a bounded executor) or accept it's synchronous.
- **P6 (Medium):** Missing/ineffective indexes — see below.

---

## 🗄️ INDEXES & SCHEMA

- **I1 (Medium):** `Quote` has one composite index `(slug, authorId, themeId)`. It's only usable left-most-first, so `WHERE author.id=…`, `WHERE theme.isActive=…`, and `status` filters don't benefit; `LIKE '%kw%'` can't use any B-tree index. **Fix:** separate indexes on `status`, `theme_id`, `author_id`; a unique index on `slug`; for search consider full-text / `pg_trgm`.
- **I2 (Medium):** No unique constraint on `Quote.slug` or `Author.slug` (Theme.slug is unique) → slug collisions; and `findBySlug` may return arbitrary rows.
- **I3 (Medium):** No unique constraint on `Favorite(user_id, quote_id)` → duplicate favorites possible (ties to C7).
- **I4 (Low):** `Theme` index `(slug, name, isActive)` is a single composite with the same left-most limitation.

---

## 🔁 TRANSACTION BOUNDARIES

- **T1 (Medium):** Inconsistent: `QuoteServiceimpl.save/update/disable/enable` have **no** `@Transactional`; `AuthorServiceImpl`/`ThemeServiceImpl` use `jakarta.transaction.Transactional` while `JwtService` uses `org.springframework…Transactional`. Multi-step ops (e.g. author disable → bulk quote status update) aren't guaranteed atomic. **Fix:** standardize on `org.springframework.transaction.annotation.Transactional`, annotate all write methods; `@Transactional(readOnly=true)` on reads.
- **T2 (Medium):** `ThemeServiceImpl.disable/enable` contain `new RuntimeException(...)` that is **constructed but never thrown** (`ThemeServiceImpl.java:31,42`) — the "already disabled/enabled" guard is dead code, so invalid state transitions proceed silently.
- **T3 (Low):** `@Modifying` bulk updates (`changeStatusByAuthor/Theme`) leave in-memory entities stale within the same transaction.

---

## 🔀 DTO MAPPING INCONSISTENCIES

- **M1 (High):** Mappers instantiate other mappers with `new AuthorMapperImpl()` / `new ThemeMapperImpl()` / `new QuoteMapperImpl()` (`QuoteMapperImpl:27-28`, `ThemeMapperImpl:29`) instead of injecting Spring beans — this is what enables the C1 recursion and defeats DI/reuse. **Fix:** inject collaborators; or adopt MapStruct (your standards mention it).
- **M2 (Medium):** `AuthMapperImpl.toDto` puts the **`Role` JPA entity** straight into `UserDTOResponse` (`UserDTOResponse.role`) — leaks an entity into the API and risks lazy-serialization issues. **Fix:** expose a role name string.
- **M3 (Medium):** `QuoteMapperImpl.toResponse` always passes `null` for `createdByUser`; `FavoriteMapperImpl.toDto` always passes `null` for `quote` — responses omit the very data they advertise.
- **M4 (Low):** `QuoteMapperImpl.toEntityUpdate` will NPE if `quote.getContent()` is null; broadly, update mappers don't handle partial updates consistently (Author's `updateEntity` overwrites with possibly-null values).

---

## 📚 REPOSITORY QUERY ISSUES

- **R1 (Low/Med):** `RoleRepository.findByName(Enum<UserRole> name)` — wrong parameter type (`Enum<UserRole>` instead of `UserRole`); works by erasure but is misleading.
- **R2 (Low):** `AuthorRepository.findById(Integer)` redundantly redeclares the inherited method.
- **R3 (Low):** `JwtRepository` typo `findByRefrechToken` and HQL without `SELECT`; naming unclear (`findByUserValidToken`).
- **R4 (Low):** `OtpCodeRepository.findByOtpAndUserEmail` is fine, but OTP lookups should also filter unexpired/unconsumed (ties to S5).

---

## 📖 SWAGGER / OPENAPI GAPS

- **D1 (Medium):** No `@SecurityScheme`(bearer JWT) / `@SecurityRequirement` → Swagger UI can't authorize requests; no global `OpenAPI` bean (title/version/servers). No `@Tag`/`@Operation`/`@ApiResponse` on controllers, so error shapes and status codes are undocumented. **Fix:** add an OpenAPI config bean with a bearer scheme and annotate controllers.
- **D2 (Low):** Spring Data REST (C4) publishes its own HAL endpoints/docs, muddying the documented API surface — another reason to remove it.

---

## ❗ ERROR HANDLING INCONSISTENCIES

- **E1 (High):** `@ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})` declares parameter `EntityNotFoundException ex`, but `UsernameNotFoundException` is **not** a subtype (it extends `AuthenticationException`). Same defect in `@ExceptionHandler({AccessDeniedException.class, IllegalStateException.class})` with param `AccessDeniedException`. Spring can't bind the declared parameter for the non-matching type → the handler fails for those exceptions. **Fix:** split handlers, or widen the parameter to a common supertype.
- **E2 (Medium):** `IllegalStateException` (business "already disabled" states) is mapped to **403 Forbidden** — semantically a 409 Conflict / 400. Business conflicts shouldn't read as authz failures.
- **E3 (Medium):** Inconsistent exception types: `JwtService.refrechToken` throws `RuntimeException` in one branch (→500) and `BusinessException` (→400) in another; `AuthServiceImpl.register` throws `EntityExistsException` for a *missing* role.
- **E4 (Medium):** `EmailTransporter` swallows all exceptions with `System.err.println` (`:60`) — mail failures are invisible. **Fix:** log via SLF4J, consider a typed `NotificationException`.
- **E5 (Medium):** Catch-all returns `ex.getMessage()` to the client → internal detail leakage. **Fix:** generic message + correlation id; log details server-side.

---

## 🧹 DEAD CODE / UNUSED

- **DC1:** `Tag` and `Notification` entities — no repositories, no usage (features stubbed but absent).
- **DC2:** `OtpCodeServiceImpl.confirmOtp`/`showValidation` and `ResetPasswordServiceImpl.validResetPassword` are empty/`null` stubs.
- **DC3:** `MotivoraApplication` unused import `org.springframework.security.config.annotation.web.SecurityMarker`.
- **DC4:** `@NoArgsConstructor` + `@AllArgsConstructor` on controllers (`AdminQuoteController`, `FavoriteController`) allow constructing them with null dependencies — drop them; rely on constructor injection.
- **DC5:** `RegisterDTORequest.pseudo` field is never used (pseudo is built from first+last). Numerous unused imports (`AuthController` imports `Collection`, `Collections`, `GetMapping`, `RequestParam`, etc.). Commented-out code in many files. `LogoutController.hello` / `TestController` debug endpoints.
- **Priority:** Low–Medium (cleanup).

---

## ♻️ CODE DUPLICATION

- **DU1:** `findByIdOrSlug` is reimplemented in Quote/Author/Theme services — extract a generic helper.
- **DU2:** `UniqueFieldValidator` and `ExistFieldValidator` are near-identical (differ only by `count==0` vs `count==1`) — share a base.
- **DU3:** Token-disabling logic repeated across `logout`/`refrechToken`/`disableTokens`.
- **Priority:** Low.

---

## 🏷️ NAMING INCONSISTENCIES

`toogleFavoriteQuote`, `refrechToken`/`findByRefrechToken` (typos); `QuoteServiceimpl` (lowercase i); `first_name`/`last_name` (snake_case in Java); `create()` methods that actually perform a GET-by-id (`AuthorService.create`, `ThemeService.create`, controllers); French/English mix in package names (`transporteur`), messages, and logs; `ConfigSecurityApplication`/`ConfigEncodingPassword` awkward names.
**Priority:** Low–Medium (readability/maintainability).

---

## 🧪 MISSING TESTS

Only `contextLoads()` exists. **Impact:** None of the above bugs would be caught; refactoring is unsafe. **Solution:** unit tests for services + validators + mappers (the recursion and null-mapping bugs are trivially caught); `@DataJpaTest` for repositories/queries; `@SpringBootTest`/MockMvc for auth, RBAC, and the public/protected split; security tests for token expiry/forgery.
**Priority:** High.

---

## 🧩 MISSING BUSINESS FEATURES

- Password-reset completion endpoint + email verification (`User.emailVerifiedAt` is never set).
- User profile / `GET /me`.
- Public authors/themes listing + pagination (currently admin-only and unpaginated).
- Quote delete and full author/theme lifecycle (only enable/disable).
- User-scoped favorites listing + pagination (current one leaks all users — C7).
- Rate limiting on auth + OTP (brute-force / email-bombing protection).
- Refresh-token rotation with reuse detection.
- The `Tag` and `Notification` features implied by the entities.
- **Priority:** Mixed (favorites scoping = Critical via C7; reset/verification = High; rest = Medium/Low).

---

## 📊 SCORECARD

| Dimension | Score | Rationale |
|---|---|---|
| **Architecture** | **5.0 / 10** | Sound layering and a genuinely good notification strategy pattern, but no migrations, mappers built with `new` (causing C1), entities leaked in DTOs, and Spring Data REST undermining the layered design. |
| **Security** | **2.0 / 10** | Data REST exposes User/Jwt/OtpCode (C4), zero RBAC enforcement (C5), hardcoded secret (S1), token logging (S2), broken reset (S5), cross-user favorites leak (C7), 500 on bad credentials (S3). |
| **Maintainability** | **4.0 / 10** | Consistent structure, but pervasive typos, dead code/stubs, FR/EN mix, duplication, and essentially no tests. |
| **Performance** | **3.0 / 10** | N+1 by default, mapping recursion, load-all "random", synchronous email, `open-in-view`, weak/missing indexes. |
| **Production Readiness** | **2.0 / 10** | Core flows are broken (quote create, all read endpoints crash, public endpoints locked), no migrations, no tests, multiple security holes. |

**Overall: ~3.2 / 10 — early prototype, not deployable.**

---

## 🗺️ ACTIONABLE ROADMAP

### Phase 0 — Make it run correctly (Critical, do first)
1. Fix `@EntityListeners(QuoteSlug.class)` on `Quote` (C3).
2. Add `themeId` to `QuoteRequest`; resolve+set author/theme/creator in the service; creator from principal (C2, S8).
3. Break the mapper recursion; inject mappers instead of `new` (C1, M1).
4. Remove `spring-boot-starter-data-rest` + all `@RepositoryRestResource` (C4).
5. Enforce RBAC on `/admin/**`; open `GET /quotes/**` (C5, C6).
6. Scope favorites to the current user; `@ManyToOne` + unique `(user,quote)` (C7).

### Phase 1 — Security hardening (High)
7. Externalize JWT secret; remove token logging; lower security log level (S1, S2).
8. Add `AuthenticationException` handler (401) and try/catch in `JwtFilter` (S3, S4).
9. Fix the `@ExceptionHandler` parameter-binding defects and status-code semantics (E1, E2, E3).
10. Complete the password-reset flow with expiry/single-use + endpoint; add rate limiting (S5).
11. Prod cookie/CORS settings; lock down actuator (S6, S7).

### Phase 2 — Data & performance (High/Medium)
12. Introduce Flyway, switch `ddl-auto` to `validate` (P2).
13. Fix associations to LAZY + `@EntityGraph`/projections; disable `open-in-view` (P1, P4).
14. Add indexes + unique slug/favorite constraints; fix random quote query (I1–I4, P3).
15. Standardize `@Transactional`; remove the dead `RuntimeException` guards (T1, T2).

### Phase 3 — Quality & docs (Medium/Low)
16. Test suite: services, validators, mappers, repositories, auth/RBAC (Missing Tests).
17. OpenAPI security scheme + annotations (D1).
18. Validation fixes (favorites controller, `@Valid` on reset, name rules), fix `null`-mapping DTOs (V1–V4, M2–M3).
19. Cleanup: dead code, typos, naming, duplication (DC/DU/Naming).
20. Build out missing features (verification, profile, public listings, pagination).

---

**Note:** No application code was modified to produce this audit. The fastest path to a working app is **Phase 0**; without it, the read API crashes and quote creation/favorites are broken regardless of other improvements.
