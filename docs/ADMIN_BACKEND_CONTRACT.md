# Contrat backend — Espace d'administration Motivora

Ce document décrit **exactement** les endpoints que le front admin appelle déjà
(via `src/services/admin.service.ts`). Le front est codé contre ce contrat :
**une fois ces endpoints livrés conformément à ce document, aucune modification
du front ne sera nécessaire.** Les requêtes qui renvoient aujourd'hui un `404`
fonctionneront automatiquement.

---

## 1. Conventions générales

- **Base URL** : `${BACKEND_URL}/api` (ex. `http://localhost:8080/api`).
- **Authentification** : `Authorization: Bearer <accessToken>` (géré par
  `authenticatedFetch`, avec refresh automatique sur `401`).
- **Autorisation** : toutes les routes `/admin/**` (et `/admin/users/**`)
  **doivent exiger le rôle `ADMIN`**. Le champ `role` de `UserDTOResponse` doit
  valoir exactement `"ADMIN"` pour un administrateur (constante front
  `ADMIN_ROLE` dans `src/types/admin.ts`). Si vous utilisez `ROLE_ADMIN`,
  signalez-le : un seul mot à changer côté front.
- **Pagination** (forme Spring `Page` déjà utilisée par `/quotes`) :

```json
{
  "content": [ /* … */ ],
  "number": 0,
  "size": 20,
  "totalElements": 134,
  "totalPages": 7,
  "first": true,
  "last": false,
  "empty": false
}
```

- **Erreurs** : tout statut `>= 400` est traité comme un échec côté front
  (message générique). Préférez `401/403` pour l'auth, `404` pour introuvable,
  `409` pour conflit (ex. doublon), `422` pour validation.

Légende : ✅ existe déjà · ⚠️ **à implémenter** · 🆕 ajout demandé sur un schéma existant.

---

## 2. Thèmes — `theme-controller` (✅ complet)

| Méthode & route | Corps | Réponse | Fonction front |
|---|---|---|---|
| `GET /admin/themes` | — | `ThemeResponse[]` | `listThemes()` |
| `GET /admin/themes/{idOrSlug}` | — | `ThemeResponse` | `getTheme()` |
| `POST /admin/themes` | `ThemeRequest` | `ThemeResponse` | `createTheme()` |
| `PUT /admin/themes/{idOrSlug}` | `ThemeRequest` | `ThemeResponse` | `updateTheme()` |
| `PUT /admin/themes/{idOrSlug}/enable` | — | `ThemeResponse \| {}` | `setThemeActive(.., true)` |
| `PUT /admin/themes/{idOrSlug}/disable` | — | `ThemeResponse \| {}` | `setThemeActive(.., false)` |

```
ThemeRequest  { name: string(3..50), description?: string(0..500),
                color: string /^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$/, imageUrl?: string }
ThemeResponse { id, slug, name, description?, color, imageUrl?, isActive: boolean }
```

> RAS — le module Thèmes est pleinement fonctionnel dès maintenant.

---

## 3. Auteurs — `author-controller` (⚠️ liste + `isActive` manquants)

| Méthode & route | Corps | Réponse | Fonction front | État |
|---|---|---|---|---|
| `GET /admin/authors?page&size` | — | `Page<AuthorResponse>` | `listAuthors()` | ⚠️ **à implémenter** |
| `GET /admin/authors/{idOrSlug}` | — | `AuthorResponse` | `getAuthor()` | ✅ |
| `POST /admin/authors` | `AuthorRequest` | `AuthorResponse` | `createAuthor()` | ✅ |
| `PUT /admin/authors/{idOrSlug}` | `AuthorRequest` | `AuthorResponse` | `updateAuthor()` | ✅ |
| `PUT /admin/authors/{idOrSlug}/enable` | — | `AuthorResponse \| {}` | `setAuthorActive(.., true)` | ✅ |
| `PUT /admin/authors/{idOrSlug}/disable` | — | `AuthorResponse \| {}` | `setAuthorActive(.., false)` | ✅ |

```
AuthorRequest  { name: string(2..100), bio?: string(0..1000), avatarUrl?: string }
AuthorResponse { id, name, slug, bio?, avatarUrl?, isActive?: boolean }   // 🆕 ajouter isActive
```

> **Bloquant n°1** : sans `GET /admin/authors`, impossible de choisir un
> `authorId` pour créer une citation. Pagination attendue (forme `Page`).
> Ajouter `isActive` à `AuthorResponse` pour afficher le statut et l'action
> activer/désactiver.

---

## 4. Citations — `admin-quote-controller` (⚠️ liste admin manquante)

| Méthode & route | Corps | Réponse | Fonction front | État |
|---|---|---|---|---|
| `GET /admin/quotes?page&size&status` | — | `Page<QuoteResponse>` | `listQuotes()` | ⚠️ **à implémenter** |
| `GET /quotes/{idOrSlug}` | — | `QuoteResponse` | `getQuote()` | ✅ (public) |
| `POST /admin/quotes` | `QuoteRequest` | `QuoteResponse` | `createQuote()` | ✅ |
| `PUT /admin/quotes/{idOrSlug}` | `QuoteRequest` | `QuoteResponse` | `updateQuote()` | ✅ |
| `PUT /admin/quotes/{idOrSlug}/enable` | — | `QuoteResponse \| {}` | `setQuoteActive(.., true)` | ✅ |
| `PUT /admin/quotes/{idOrSlug}/disable` | — | `QuoteResponse \| {}` | `setQuoteActive(.., false)` | ✅ |

```
QuoteRequest  { content: string(5..500), authorId: number, themeId: number }
QuoteResponse { id, slug, content, author: AuthorResponse, theme: ThemeResponse,
                createdByUser?: UserDTOResponse, status: string }
```

> `GET /admin/quotes` doit **inclure tous les statuts** (publiées, en attente,
> désactivées) — contrairement au `GET /quotes` public — pour la modération.
> Paramètre `status` optionnel pour filtrer.
>
> **Valeurs de `status` attendues** par le front (sinon le badge affiche la
> valeur brute) : `PUBLISHED` / `ACTIVE` (publiée), `PENDING` (en attente),
> `DISABLED` / `INACTIVE` / `ARCHIVED` (désactivée). L'action « Activer/
> Désactiver » mappe vers `/enable` et `/disable`.

---

## 5. Utilisateurs — `admin-user-controller` (implémenté récemment — à confirmer conforme)

| Méthode & route | Corps | Réponse | Fonction front |
|---|---|---|---|
| `GET /admin/users?page&size&search` | — | `Page<UserDTOResponse>` | `listUsers()` |
| `PUT /admin/users/{id}/status` | `{ "status": "ACTIVE" \| "SUSPENDED" }` | `UserDTOResponse \| {}` | `setUserStatus()` |
| `PUT /admin/users/{id}/role` | `{ "role": "USER" \| "ADMIN" }` | `UserDTOResponse \| {}` | `setUserRole()` |

```
UserDTOResponse { id, pseudo, email,
                  status: "ACTIVE" | "INACTIVE" | "SUSPENDED" | "DELETED",
                  role: string }
```

> Le front appelle ces routes telles quelles. Si votre implémentation diffère
> (ex. `/suspend` + `/activate` au lieu d'un `/status`, ou un autre nom de
> paramètre), **alignez-vous sur ce contrat** OU indiquez les écarts : seules
> 2 lignes de `admin.service.ts` (`setUserStatus`, `setUserRole`) seraient à
> ajuster. `search` est optionnel (recherche pseudo/email).

---

## 6. Statistiques — `admin-stats-controller` (⚠️ optionnel mais recommandé)

| Méthode & route | Réponse | Fonction front |
|---|---|---|
| `GET /admin/stats` | `AdminStats` | `getStats()` |

```
AdminStats { totalQuotes: number, totalAuthors: number, totalThemes: number,
             totalUsers: number, pendingQuotes?: number }
```

> Tant que l'endpoint n'existe pas, la page « Vue d'ensemble » **retombe
> automatiquement** sur les compteurs `totalElements` des listes (et la longueur
> de `/admin/themes`). Aucune action requise pour que le dashboard reste
> fonctionnel, mais cet endpoint donnera des chiffres exacts en une requête + un
> compteur `pendingQuotes` (modération).

---

## 7. Récapitulatif des manques bloquant/limitant

| Priorité | Manque | Impact si absent |
|---|---|---|
| 🔴 Haute | `GET /admin/authors` (paginé) | Création de citation impossible (pas de picker d'auteur) ; liste auteurs vide |
| 🟠 Moyenne | `GET /admin/quotes` (tous statuts) | Pas de modération des citations désactivées/en attente |
| 🟠 Moyenne | `GET /admin/users` + `/status` + `/role` | Module utilisateurs vide / actions sans effet (dit implémenté — **à vérifier conforme**) |
| 🟡 Basse | `isActive` dans `AuthorResponse` | Statut auteur toujours affiché « Actif » |
| 🟡 Basse | `GET /admin/stats` | Vue d'ensemble en repli sur compteurs de listes (déjà géré) |

Une fois ces points livrés **selon les chemins, méthodes et schémas ci-dessus**,
le front admin sera 100% opérationnel sans aucune retouche.
