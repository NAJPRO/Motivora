# Gestion des utilisateurs par l'administrateur

Ce document décrit l'implémentation backend du module **administration des
utilisateurs** (`admin-user-controller`). Il est conforme à la section 5 du
contrat `ADMIN_BACKEND_CONTRACT.md`.

- **Base URL** : `${BACKEND_URL}/api`
- **Sécurité** : toutes les routes sont sous `/admin/**` → exigent le rôle
  `ADMIN` (`ConfigSecurityApplication` : `.requestMatchers("/admin/**").hasRole("ADMIN")`).
- **Format du rôle** : `UserDTOResponse.role` vaut exactement `"ADMIN"`,
  `"USER"`, `"MODERATOR"` ou `"CREATOR"` (nom de l'enum `UserRole`, **sans**
  préfixe `ROLE_`). Conforme à la constante front `ADMIN_ROLE`.

---

## 1. Endpoints

| Méthode & route | Corps | Réponse | Code | Description |
|---|---|---|---|---|
| `GET /admin/users?page&size&search` | — | `Page<UserDTOResponse>` | 200 | Liste paginée ; `search` (optionnel) filtre sur pseudo **ou** email (insensible à la casse) |
| `GET /admin/users/{id}` | — | `UserDTOResponse` | 200 / 404 | Détail d'un utilisateur |
| `POST /admin/users` | `AdminCreateUserRequest` | `UserDTOResponse` | 201 | Création d'un compte avec rôle choisi |
| `PUT /admin/users/{id}/role` | `UpdateUserRoleRequest` | `UserDTOResponse` | 200 | Modifie le rôle |
| `PUT /admin/users/{id}/status` | `UpdateUserStatusRequest` | `UserDTOResponse` | 200 | Modifie le statut |
| `DELETE /admin/users/{id}` | — | `{ "message": "User deleted successfully" }` | 200 | Suppression **logique** (statut `DELETED`) |

> Les routes `/role` et `/status` correspondent exactement au contrat front
> (`setUserRole`, `setUserStatus`). `GET`, `POST`, `DELETE` sont des ajouts
> cohérents pour un CRUD complet.

---

## 2. DTOs

### Requêtes

```jsonc
// AdminCreateUserRequest
{
  "pseudo":   "string (2..50, requis)",
  "email":    "string email, requis, unique",     // @UniqueField(User.email)
  "password": "string (>=8, 1 maj + 1 min + 1 chiffre, requis)",
  "role":     "USER | ADMIN | MODERATOR | CREATOR" // requis
}

// UpdateUserRoleRequest
{ "role": "USER | ADMIN | MODERATOR | CREATOR" }   // requis

// UpdateUserStatusRequest
{ "status": "ACTIVE | INACTIVE | SUSPENDED | DELETED" } // requis
```

### Réponse

```jsonc
// UserDTOResponse
{
  "id":     1,
  "pseudo": "Alice",
  "email":  "alice@example.com",
  "status": "ACTIVE | INACTIVE | SUSPENDED | DELETED",
  "role":   "ADMIN"   // chaîne (nom de l'enum), jamais l'entité Role
}
```

La validation échouée renvoie `400` avec une map `{ champ: message }`.
Email en doublon : `409` (via `EntityExistsException` / contrainte d'unicité).

---

## 3. Règles métier & sécurité

1. **Le statut est appliqué à l'authentification.** `User.isEnabled()` renvoie
   `status == ACTIVE` et `User.isAccountNonLocked()` renvoie `status != SUSPENDED`.
   Un compte `INACTIVE`, `SUSPENDED` ou `DELETED` ne peut donc plus se connecter
   (échec d'authentification → `401`).

2. **Révocation immédiate des sessions** (`JwtService.disableTokens`) :
   - lors d'un **changement de rôle** (changement de privilège → un nouveau token
     doit être émis) ;
   - lors d'un passage à un statut **non actif** ;
   - lors d'une **suppression**.

   Les tokens existants en base sont marqués expirés ; l'utilisateur doit se
   reconnecter.

3. **Protection anti-auto-blocage.** Un administrateur **ne peut pas** modifier
   son propre rôle/statut ni se supprimer (`BusinessException` → `400`). Cela
   évite qu'un admin se retire ses droits ou se verrouille hors de l'application.

4. **Suppression logique.** `DELETE` positionne `status = DELETED` (et révoque
   les tokens) au lieu de supprimer la ligne, afin de préserver l'intégrité
   référentielle (citations créées, favoris, tokens, codes OTP).

---

## 4. Architecture / fichiers

```
controller/Admin/AdminUserController.java   ← endpoints REST (thin controller)
service/UserService.java                    ← interface
service/Impl/UserServiceImpl.java           ← logique métier + transactions
dto/request/AdminCreateUserRequest.java
dto/request/UpdateUserRoleRequest.java
dto/request/UpdateUserStatusRequest.java
dto/response/UserDTOResponse.java           ← réutilisé (mapping via AuthMapper.toDto)
entity/User.java                            ← isEnabled()/isAccountNonLocked() câblés sur le statut
```

Dépendances injectées dans `UserServiceImpl` : `UserRepository`, `RoleRepository`,
`BCryptPasswordEncoder`, `AuthMapper`, `JwtService`, `AuthUtil`.

---

## 5. Tests

`UserServiceImplTest` (Mockito) couvre : création (encodage du mot de passe,
rôle, statut `ACTIVE`), changement de rôle + révocation des tokens, passage de
statut non-actif → révocation, statut `ACTIVE` → conservation des sessions,
suppression logique, gardes anti-auto-blocage (rôle/suppression), `404`
introuvable.

---

## 6. Amorçage d'un administrateur

Aucun compte `ADMIN` n'est créé par l'inscription publique (qui assigne `USER`).
`AdminSeeder` crée un admin au démarrage **si** `MOTIVORA_ADMIN_EMAIL` et
`MOTIVORA_ADMIN_PASSWORD` sont définis et que le compte n'existe pas encore.
