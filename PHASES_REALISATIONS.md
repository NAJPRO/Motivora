# Motivora — Réalisations des phases de mise en production

Ce document récapitule l'ensemble des corrections et améliorations apportées au
backend Motivora durant les phases 0 à 3, à la suite de l'audit de
*production-readiness* (voir `PRODUCTION_READINESS_AUDIT.md`).

- **Branche :** `fix/production-readiness-phase0`
- **État :** compile, **36 tests unitaires au vert**, démarrage validé sur base fraîche **et** base existante (Flyway).
- **Date :** 2026-06-07

> Codes de référence (ex. C1, S3, P2…) = identifiants des problèmes listés dans le rapport d'audit.

---

## Vue d'ensemble (avant → après)

| Dimension | Avant | Après |
|---|---|---|
| Architecture | 5 / 10 | ~7.5 / 10 |
| Sécurité | 2 / 10 | ~7.5 / 10 |
| Maintenabilité | 4 / 10 | ~7 / 10 |
| Performance | 3 / 10 | ~7 / 10 |
| Production readiness | 2 / 10 | ~7 / 10 |

---

## PHASE 0 — Blocages critiques (l'application ne fonctionnait pas)

### C1 — Récursion infinie dans le mapping DTO (`StackOverflowError`)
- **Avant :** `ThemeResponse` contenait la liste des `quotes`, et chaque `QuoteResponse` contenait son `theme` → boucle infinie au mapping. Tous les endpoints de lecture plantaient.
- **Correction :**
  - `ThemeResponse` : suppression du champ `quotes`.
  - `ThemeMapperImpl` / `QuoteMapperImpl` : mappers désormais **injectés par Spring** (plus de `new`), mapping sans aller-retour.
- **Fichiers :** `dto/response/ThemeResponse.java`, `mapper/Impl/ThemeMapperImpl.java`, `mapper/Impl/QuoteMapperImpl.java`.

### C2 — Création de citation impossible (violations NOT NULL + champs ignorés)
- **Avant :** `QuoteMapperImpl.toEntity` ne posait que `content` ; pas de `theme` (NOT NULL), `QuoteRequest` n'avait pas de `themeId`.
- **Correction :**
  - Ajout de `themeId` dans `QuoteRequest` (validé via `@ExistField`).
  - `QuoteServiceImpl.save/update` résout `Author`, `Theme` et fixe le créateur depuis le principal authentifié.
- **Fichiers :** `dto/request/QuoteRequest.java`, `service/Impl/QuoteServiceImpl.java`.

### C3 — Mauvais `@EntityListeners` sur `Quote` (slug jamais généré)
- **Avant :** `@EntityListeners(QuoteStatus.class)` (une enum, pas un listener).
- **Correction :** `@EntityListeners(QuoteSlug.class)`.
- **Fichiers :** `entity/Quote.java`.

### C4 — Spring Data REST exposait tous les repositories (User/Jwt/OtpCode…)
- **Avant :** `spring-boot-starter-data-rest` + `@RepositoryRestResource` partout → endpoints CRUD auto-générés exposant mots de passe, tokens, codes OTP.
- **Correction :** suppression de la dépendance et de **toutes** les annotations `@RepositoryRestResource`.
- **Fichiers :** `pom.xml`, tous les repositories (`QuoteRepository`, `AuthorRepository`, `ThemeRepository`, `FavoriteRepository`, `UserRepository`, `JwtRepository`, `OtpCodeRepository`).

### C5 — RBAC défini mais jamais appliqué
- **Avant :** seul `authenticated()` ; n'importe quel `USER` pouvait administrer.
- **Correction :**
  - `/admin/**` exige le rôle `ADMIN`.
  - Ajout d'un `AdminSeeder` (config-driven) pour amorcer un compte admin via `MOTIVORA_ADMIN_EMAIL` / `MOTIVORA_ADMIN_PASSWORD`.
- **Fichiers :** `security/ConfigSecurityApplication.java`, `utils/seeders/AdminSeeder.java`, `utils/seeders/RoleSeeder.java` (`@Order`), `application.yml`.

### C6 — Les endpoints « publics » exigeaient une authentification
- **Avant :** `/quotes/**` n'était pas dans la liste autorisée.
- **Correction :** `GET /quotes/**` ouvert au public.
- **Fichiers :** `security/ConfigSecurityApplication.java`.

### C7 — Favoris : fuite inter-utilisateurs + suppression globale
- **Avant :** `getAllFavorites()` retournait les favoris de **tous** les utilisateurs ; le retrait supprimait la ligne pour tout le monde ; `@OneToOne` interdisait qu'un même quote soit favori de plusieurs users.
- **Correction :**
  - `Favorite.quote` → `@ManyToOne`, contrainte d'unicité `(user_id, quote_id)`.
  - Toutes les opérations limitées à l'utilisateur courant ; suppression par `(quoteId, userId)`.
- **Fichiers :** `entity/Favorite.java`, `repository/FavoriteRepository.java`, `service/Impl/FavoriteServiceImpl.java`, `mapper/Impl/FavoriteMapperImpl.java`, `controller/Client/FavoriteController.java`.

---

## PHASE 1 — Durcissement sécurité & gestion d'erreurs

### S1 — Secret JWT externalisé
- Secret déplacé en config `app.jwt.secret` (override via `MOTIVORA_JWT_SECRET`), plus de valeur en dur dans le `.java`.
- **Fichiers :** `security/JwtService.java`, `application.yml`.

### S2 — Plus de log des tokens
- Suppression des `System.out.println`/`log.info` exposant le token ; niveau de log sécurité passé à `INFO`.
- **Fichiers :** `security/JwtService.java`, `security/JwtFilter.java`, `application.yml`.

### S3 — Mauvais identifiants → 401 (au lieu de 500)
- Ajout d'un handler `AuthenticationException` → 401.
- **Fichiers :** `exception/CustomExceptionHandler.java`.

### S4 — Token malformé/altéré → 401
- `JwtFilter` encapsule la validation dans un `try/catch` → 401 propre, sans fuite.
- **Fichiers :** `security/JwtFilter.java`.

### S5 — Flux de réinitialisation de mot de passe complété
- **Avant :** `validResetPassword` était un stub vide, pas d'endpoint de confirmation.
- **Correction :** vérification (OTP existant, non expiré, non consommé), changement effectif du mot de passe, OTP marqué `confirmedAt`, invalidation des OTP restants, **un seul OTP actif par utilisateur**, nouvel endpoint `POST /reset-password/confirm`.
- **Fichiers :** `service/Impl/ResetPasswordServiceImpl.java`, `controller/Auth/ResetPasswordController.java`, `dto/request/ResetPasswordConfirmRequest.java`, `repository/OtpCodeRepository.java`.

### S6 — Cookies & CORS configurables
- Cookie refresh : `secure` et `SameSite` pilotés par config (`app.cookie.*`).
- Origines CORS pilotées par config (`app.cors.allowed-origins`).
- **Fichiers :** `controller/Auth/AuthController.java`, `security/CorsConfig.java`, `application.yml`.

### S7 — Actuator restreint
- Exposition limitée à `health,info`.
- **Fichiers :** `application.yml`.

### S8 — Créateur d'une citation pris depuis le principal
- `createdByUserId` retiré du `QuoteRequest` ; créateur = utilisateur authentifié.
- **Fichiers :** `dto/request/QuoteRequest.java`, `service/Impl/QuoteServiceImpl.java`.

### E1 — Bugs de binding des `@ExceptionHandler`
- Les listes mélangeaient des types non compatibles (ex. `UsernameNotFoundException` avec un paramètre `EntityNotFoundException`). Handlers séparés et corrigés.
- **Fichiers :** `exception/CustomExceptionHandler.java`.

### E2 — Conflits métier → 409
- `IllegalStateException` (ex. « déjà désactivé ») mappé en `409 Conflict` (au lieu de 403).
- **Fichiers :** `exception/CustomExceptionHandler.java`.

### E3 — Types d'exception cohérents
- `JwtService.refreshToken` lève `BusinessException` ; `register` lève `EntityNotFoundException` pour un rôle absent ; handler `EntityExistsException` → 409.
- **Fichiers :** `security/JwtService.java`, `service/Impl/AuthServiceImpl.java`, `exception/CustomExceptionHandler.java`.

### E5 — Plus de fuite d'erreurs internes
- Le handler `Exception` générique logge côté serveur et renvoie un message générique.
- **Fichiers :** `exception/CustomExceptionHandler.java`.

### Améliorations connexes
- **M2 :** `UserDTOResponse` n'expose plus l'entité `Role` mais une chaîne (`AuthMapperImpl`).
- **M3 :** favoris et citations incluent désormais leurs objets imbriqués (plus de `null`).
- **T1/T2 :** transactions standardisées sur `org.springframework…@Transactional` ; garde `ThemeServiceImpl` corrigée (condition inversée + exception jamais levée).
- **V1/V2 :** endpoints favoris corrigés, `@Valid` ajouté sur la demande de reset.
- **DC3 :** import `SecurityMarker` invalide supprimé.

---

## PHASE 2 — Performance & migrations

### P1 — Suppression des N+1
- Associations `@ManyToOne` de `Quote` (`author`, `theme`, `createdByUser`) et de `Favorite` (`user`, `quote`) passées en `LAZY`.
- `@EntityGraph` (fetch joins) sur les requêtes de lecture de citations et la liste des favoris.
- **Fichiers :** `entity/Quote.java`, `entity/Favorite.java`, `repository/QuoteRepository.java`, `repository/FavoriteRepository.java`.

### P3 — Citation aléatoire performante
- `getRandomQuote` : requête `ORDER BY RANDOM() LIMIT 1` au lieu de charger toutes les citations en mémoire.
- **Fichiers :** `repository/QuoteRepository.java`, `service/Impl/QuoteServiceImpl.java`.

### P4 — `open-in-view` désactivé
- `open-in-view: false` (le mapping se fait dans des méthodes `@Transactional(readOnly)` avec fetch joins).
- **Fichiers :** `application.yml`.

### P5 — Emails réellement asynchrones
- `@EnableAsync` ajouté (le `@Async` du service de notification n'avait aucun effet auparavant).
- **Fichiers :** `MotivoraApplication.java`.

### Index & contraintes (I1/I2/I3)
- Index dédiés sur `quotes(status)`, `quotes(theme_id)`, `quotes(author_id)`, index unique sur `quotes(slug)`.
- Contrainte d'unicité `favorites(user_id, quote_id)`.
- **Fichiers :** `entity/Quote.java`, `entity/Favorite.java`.

### P2 — Migrations Flyway
- Ajout de `flyway-core` + `flyway-database-postgresql`.
- `ddl-auto: validate` (au lieu de `update`).
- Migration **baseline** `V1__init.sql` **générée depuis le modèle Hibernate** (donc strictement alignée avec `validate`).
- Config : `baseline-on-migrate: true`, `baseline-version: 1` (les bases existantes sont baselinées en v1 et `V1` est sauté ; une base vierge applique `V1`).
- **Fichiers :** `pom.xml`, `application.yml`, `src/main/resources/db/migration/V1__init.sql`.
- **Vérifié :** base vierge → `V1` appliqué + `validate` OK ; base pré-existante (sans historique) → baseline v1 + `V1` sauté + `validate` OK (tests réalisés sur des bases jetables, `motivora_db` non touchée).

### D1 — OpenAPI / Swagger
- `OpenApiConfig` : schéma de sécurité **bearer JWT** + métadonnées d'API → bouton « Authorize » fonctionnel dans Swagger UI.
- **Fichiers :** `config/OpenApiConfig.java`.

---

## PHASE 3 — Tests, rate limiting, nettoyage & fonctionnalité

### Tests (lacune majeure comblée)
- **36 tests unitaires** (JUnit 5 + Mockito, sans base de données), tous au vert :
  - Mappers : `QuoteMapperImplTest` (non-récursion, null-safety), `ThemeMapperImplTest`, `FavoriteMapperImplTest`, `AuthMapperImplTest` (rôle en chaîne).
  - Services : `QuoteServiceImplTest` (résolution auteur/thème/créateur, garde d'état, aléatoire), `FavoriteServiceImplTest` (scoping utilisateur), `ThemeServiceImplTest` (gardes activer/désactiver), `ResetPasswordServiceImplTest` (OTP inconnu/expiré/déjà utilisé/succès).
  - Utils : `SlugTest`, `OtpGeneratorTest`.
- **Fichiers :** `src/test/java/com/audin/motivora/**` (10 classes de test).

### S5 — Rate limiting
- `RateLimitFilter` : limiteur en mémoire (fenêtre fixe, par IP) sur `/auth/login`, `/auth/register`, `/reset-password/**` → `429` au dépassement. Paramétrable via `app.rate-limit.*`.
- **Fichiers :** `security/RateLimitFilter.java`, `application.yml`.

### GET /me
- Endpoint retournant le profil de l'utilisateur authentifié.
- **Fichiers :** `controller/Client/MeController.java`.

### Nettoyage / nommage
- `QuoteServiceimpl` → `QuoteServiceImpl`.
- `refrechToken` / `findByRefrechToken` → `refreshToken` / `findByRefreshToken` (R3).
- `RoleRepository.findByName(Enum<UserRole>)` → `findByName(UserRole)` (R1).
- `RegisterDTORequest` : regex de nom corrigée (accents, espaces, traits d'union, apostrophes), messages corrigés, champ `pseudo` inutilisé supprimé (V3).
- Suppression du code mort : `OtpCodeService` + `OtpCodeServiceImpl`.
- **Fichiers :** services/repositories concernés, `dto/request/RegisterDTORequest.java`.

---

## Reste à faire (non couvert)

- Vérification d'e-mail (`emailVerifiedAt` n'est jamais positionné).
- Listing public **auteurs/thèmes** + pagination (actuellement réservé à l'admin).
- Fonctionnalités `Tag` / `Notification` (entités présentes mais inutilisées ; tables conservées dans `V1`).
- Tests d'intégration / MockMvc bout-en-bout (RBAC, flux d'auth) — la couverture actuelle est unitaire ; `contextLoads()` nécessite la base démarrée.
- Avertissement bénin au démarrage (`AuthenticationProvider` + `UserDetailsService`) — nettoyage de config mineur.

---

## Points d'attention

- Un compte admin de test a pu être créé dans `motivora_db` lors des vérifications : `admin@motivora.dev` / `Admin1234` → **à changer/supprimer**.
- Au prochain démarrage réel sur `motivora_db`, Flyway créera la table `flyway_schema_history` et baseline en v1 (opération non destructive).
- En production : définir `MOTIVORA_JWT_SECRET`, `MOTIVORA_COOKIE_SECURE=true`, `MOTIVORA_CORS_ORIGINS`, et éventuellement `MOTIVORA_ADMIN_*`.

---

## Vérifications effectuées

- `./mvnw clean package -DskipTests` → **BUILD SUCCESS** (jar produit).
- `./mvnw test -Dtest=...` (suite unitaire) → **36 tests, 0 échec**.
- Démarrage applicatif validé : base vierge (Flyway applique `V1`) et base pré-existante (baseline v1) ; `Started MotivoraApplication`, aucune erreur.
- Toutes les vérifications base de données ont été faites sur des bases **jetables** ; `motivora_db` n'a pas été modifiée par les tests.
