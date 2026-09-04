# Contrat d'API mobile — Motivora

Ce document décrit ce que l'application mobile (Expo / React Native) doit implémenter pour
consommer l'API, et ce qui a changé pour le front admin existant.

Base URL : `https://<host>/api/v1`

---

## 1. En-têtes envoyés par le client mobile

| En-tête | Obligatoire | Rôle |
|---|---|---|
| `Authorization: Bearer <accessToken>` | sur les routes protégées | authentification |
| `X-Device-Id` | fortement recommandé | identifiant stable par installation. Sans lui, toutes les sessions « sans appareil » d'un utilisateur se révoquent mutuellement |
| `X-Client-Platform` | recommandé | `ios` \| `android` \| `web` |
| `X-Device-Name` | optionnel | libellé affiché dans « mes appareils connectés » |

`X-Device-Id` doit être généré une fois à la première ouverture puis conservé dans
`expo-secure-store`. Il ne doit **pas** changer entre deux lancements.

---

## 2. Authentification

### Inscription / connexion

```
POST /auth/register   { first_name, last_name, email, password }   → 201
POST /auth/login      { email, password }                          → 200
```

Réponse :

```json
{
  "token": "<accessToken>",        // alias historique (front admin)
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800,                // secondes
  "data": { "id": 1, "pseudo": "...", "email": "...", "avatarUrl": null,
            "status": "ACTIVE", "role": "USER", "emailVerified": false,
            "emailVerifiedAt": null, "createdAt": "..." }
}
```

Stockage côté mobile : `accessToken` en mémoire ou SecureStore, `refreshToken`
**obligatoirement** dans SecureStore / Keychain — jamais dans AsyncStorage.

### Rafraîchissement

```
POST /auth/refresh-token   { "refreshToken": "..." }   → 200 TokenPairResponse
```

Le refresh token est **tourné à chaque usage** : la réponse en contient un nouveau, et
l'ancien devient inutilisable. Rejouer un token déjà consommé révoque **toutes** les
sessions de l'utilisateur (réponse standard à une fuite de token).

Durées : access token 30 min, refresh token 60 jours.

### Intercepteur attendu côté client

Sur `401`, rafraîchir puis rejouer **une seule fois** ; si le refresh échoue lui aussi
(401), purger le SecureStore et renvoyer vers l'écran de connexion. Les erreurs de token
renvoient toujours `401`, jamais `400`.

### Sessions

```
GET    /auth/sessions        → liste des appareils connectés (current: true sur le vôtre)
DELETE /auth/sessions/{id}   → déconnecter cet appareil
POST   /auth/logout          → déconnecter l'appareil courant
POST   /auth/logout-all      → déconnecter partout
```

### Vérification d'e-mail

```
POST /verify-email/resend    { email }         → 200 (silencieux si l'adresse est inconnue)
POST /verify-email/confirm   { email, otp }    → 200
```

Un code à 6 chiffres est envoyé automatiquement à l'inscription (validité 30 min).

---

## 3. Format des réponses

### Pagination

```json
{
  "items": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 137,
  "totalPages": 7,
  "hasNext": true,
  "hasPrevious": false
}
```

`hasNext` est ce dont a besoin une `FlatList` en infinite scroll.

### Accusé de réception

```json
{ "message": "Quote added to favorites" }
```

### Erreur

```json
{
  "code": 400,
  "error": "OTP_EXPIRED",
  "message": "This verification code has expired",
  "fieldErrors": { "email": "Email should be valid" },
  "timestamp": "2026-09-03T22:10:00"
}
```

Brancher l'UI sur `error` (code machine stable), jamais sur `message` (texte libre).

Codes principaux : `VALIDATION_FAILED`, `AUTHENTICATION_REQUIRED`, `INVALID_CREDENTIALS`,
`TOKEN_EXPIRED`, `TOKEN_INVALID`, `ACCOUNT_DISABLED`, `ACCOUNT_SUSPENDED`, `NOT_FOUND`,
`ALREADY_EXISTS`, `INVALID_STATE`, `RATE_LIMIT_EXCEEDED`, `OTP_INVALID`, `OTP_EXPIRED`,
`OTP_ALREADY_USED`, `EMAIL_ALREADY_VERIFIED`, `INVALID_CURRENT_PASSWORD`, `FILE_TOO_LARGE`,
`UNSUPPORTED_FILE_TYPE`, `INVALID_TIMEZONE`.

---

## 4. Catalogue public (accessible sans connexion)

```
GET /quotes?page&size
GET /quotes/{idOrSlug}
GET /quotes/search?keyword&page&size
GET /quotes/author/{authorId}?page&size
GET /quotes/theme/{themeIdOrSlug}?page&size
GET /quotes/random
GET /quotes/daily          ← citation du jour, identique pour tous pendant 24 h (UTC)
GET /themes                ← thèmes actifs
GET /themes/{idOrSlug}
GET /authors?page&size     ← auteurs actifs
GET /authors/{idOrSlug}
```

`QuoteResponse` :

```json
{
  "id": 12, "slug": "...", "content": "...",
  "author": { "id": 3, "name": "...", "slug": "...", "bio": "...", "avatarUrl": "...", "isActive": true },
  "theme":  { "id": 1, "slug": "...", "name": "...", "description": "...", "color": "...", "imageUrl": "...", "isActive": true },
  "status": "PUBLISHED",
  "isFavorite": false,
  "publishedAt": null,
  "createdAt": "...",
  "createdByUser": null
}
```

Ces routes acceptent un `Authorization` optionnel : si le token est fourni, `isFavorite`
est renseigné pour l'utilisateur courant — pas besoin de charger tous les favoris pour
afficher un cœur.

`createdByUser` est toujours `null` en public (il contient une adresse e-mail).

Ces routes émettent un `ETag` : renvoyer `If-None-Match` permet d'obtenir un `304` au lieu
du corps complet.

---

## 5. Compte utilisateur

```
GET    /me                      → profil
PUT    /me                      { pseudo }                            → profil mis à jour
POST   /me/password             { currentPassword, newPassword }      → déconnecte tous les appareils
DELETE /me                      { password }                          → suppression de compte
POST   /me/avatar               multipart, champ « file »             → profil mis à jour
```

`DELETE /me` est requis par la règle App Store 5.1.1(v). Les données personnelles sont
anonymisées, les favoris / appareils / notifications supprimés, l'e-mail libéré, et toutes
les sessions fermées. L'action est irréversible et confirmée par le mot de passe.

Images : JPEG, PNG ou WebP, 5 Mo maximum. Le fichier est décodé côté serveur pour vérifier
qu'il s'agit bien d'une image.

---

## 6. Favoris

```
GET    /favorites?page&size     → PageResponse<FavoriteResponse>
PUT    /favorites/{quoteId}     → 201 si créé, 200 si déjà présent (idempotent)
DELETE /favorites/{quoteId}     → 200 (idempotent)
POST   /favorites  { quoteId }  → bascule (héritage, à éviter)
```

Préférer `PUT`/`DELETE` : sur un réseau instable, un `POST` de bascule rejoué annule
silencieusement l'action précédente.

---

## 7. Notifications push

### Enregistrement de l'appareil

```
POST   /me/devices    { token, platform, deviceId, deviceName }   → à chaque démarrage de l'app
GET    /me/devices
DELETE /me/devices/{token}
```

`token` est l'Expo push token (ou le token FCM/APNs). Il tourne : réenregistrer à chaque
lancement. Un token que le fournisseur signale comme non délivrable est désactivé côté
serveur.

### Préférences

```
GET /me/notifications/preferences
PUT /me/notifications/preferences
    { dailyQuoteEnabled, dailyQuoteHour, timezone, followedThemeIds }
```

`dailyQuoteHour` est une heure **locale** (0–23), interprétée dans `timezone` (identifiant
IANA, ex. `Africa/Porto-Novo`). Envoyer le fuseau réel de l'appareil dès la première
ouverture. `followedThemeIds` vide = toutes les citations.

### Centre de notifications

```
GET /me/notifications?page&size
GET /me/notifications/unread-count      → { "unread": 3 }
PUT /me/notifications/{id}/read
PUT /me/notifications/read-all
```

Charge utile du push (lue au tap) :

```json
{ "type": "DAILY_QUOTE", "quoteId": "42", "quoteSlug": "la-vie-est-belle" }
```

---

## 8. Changements pour le front admin existant

À répercuter :

1. **URLs** — la forme canonique est `/api/v1/...`. Les anciennes URLs `/api/...`
   fonctionnent encore via un filtre de compatibilité, mais celui-ci est temporaire.
2. **Listes paginées** — `Page<T>` de Spring est remplacé par `PageResponse<T>` :
   `content` → `items`, `number` → `page`, `last` → `!hasNext`.
3. **Réponses d'action** — les corps `String` bruts (« Disable successfuly ») et les
   `Map` ad hoc sont remplacés par `{ "message": "..." }`.
4. **Erreurs** — forme unique `ApiError` ; les erreurs de validation passent de
   `{ "champ": "message" }` à `{ "error": "VALIDATION_FAILED", "fieldErrors": { ... } }`.
5. **Déconnexion** — `POST /logout` devient `POST /v1/auth/logout`.
6. **`/test/**`** — supprimé.
7. **Upload d'images admin** — `POST /admin/images/{authors|themes}` (multipart, champ
   `file`) renvoie `{ "url": "..." }` à soumettre ensuite dans `AuthorRequest` /
   `ThemeRequest`.

---

## 9. Variables d'environnement (production)

| Variable | Rôle |
|---|---|
| `MOTIVORA_DB_URL` / `_USERNAME` / `_PASSWORD` | base de données |
| `MOTIVORA_JWT_SECRET` | clé HMAC Base64, ≥ 256 bits |
| `MOTIVORA_JWT_ACCESS_TTL` / `_REFRESH_TTL` | durées ISO-8601 (`PT30M`, `P60D`) |
| `MOTIVORA_CORS_ORIGINS` | origines autorisées |
| `MOTIVORA_MAIL_HOST` / `_PORT` / `_USERNAME` / `_PASSWORD` | SMTP |
| `MOTIVORA_PUSH_PROVIDER` | `expo` ou `none` |
| `MOTIVORA_EXPO_ACCESS_TOKEN` | si la sécurité push Expo est activée |
| `MOTIVORA_STORAGE_LOCATION` / `_BASE_URL` | stockage des images |
| `MOTIVORA_ADMIN_EMAIL` / `_PASSWORD` | amorçage du compte admin |

Le profil `prod` (`SPRING_PROFILES_ACTIVE=prod`) active HTTPS-only cookies, la compression
gzip, `forward-headers-strategy` et masque les détails d'erreur.
