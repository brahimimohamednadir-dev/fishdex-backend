# FishDex — Documentation API

**Version :** Sprint 2  
**Base URL :** `http://localhost:8080`  
**Production :** à définir  

---

## Sommaire

1. [Authentification](#1-authentification)
2. [Format des réponses](#2-format-des-réponses)
3. [Codes d'erreur](#3-codes-derreur)
4. [Auth](#4-auth)
5. [Profil utilisateur](#5-profil-utilisateur)
6. [Captures](#6-captures)
7. [Photos](#7-photos)
8. [Espèces](#8-espèces)
9. [Groupes](#9-groupes)
10. [Badges](#10-badges)
11. [Modèles de données](#11-modèles-de-données)

---

## 1. Authentification

Toutes les routes sont protégées **sauf** `/api/auth/register` et `/api/auth/login`.

Ajouter le header suivant à chaque requête protégée :

```
Authorization: Bearer <token>
```

Le token est obtenu via `POST /api/auth/login`. Il est valide **24h**.

---

## 2. Format des réponses

Toutes les réponses suivent cette enveloppe :

```json
{
  "success": true,
  "message": "Message optionnel",
  "data": { ... }
}
```

En cas d'erreur :

```json
{
  "success": false,
  "message": "Description de l'erreur",
  "data": null
}
```

Les listes paginées retournent :

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "totalElements": 42,
    "totalPages": 3,
    "size": 20,
    "number": 0
  }
}
```

**Paramètres de pagination** (sur toutes les routes listées) :
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `page` | int | `0` | Numéro de page (commence à 0) |
| `size` | int | `20` | Nombre d'éléments par page |

---

## 3. Codes d'erreur

| Code | Signification |
|------|---------------|
| `400` | Données invalides (champ manquant, format incorrect) |
| `401` | Token manquant ou expiré |
| `403` | Accès interdit (mauvais propriétaire, limite freemium, non-membre) |
| `404` | Ressource introuvable |
| `409` | Conflit (email/username déjà utilisé, déjà membre d'un groupe) |
| `500` | Erreur serveur |

---

## 4. Auth

### `POST /api/auth/register`

Créer un compte utilisateur.

**Body**
```json
{
  "email": "user@fishdex.fr",
  "username": "pecheur1",
  "password": "motdepasse123"
}
```

| Champ | Type | Règles |
|-------|------|--------|
| `email` | string | Obligatoire, format email valide |
| `username` | string | Obligatoire |
| `password` | string | Obligatoire, min 8 caractères |

**Réponse `201 Created`**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@fishdex.fr",
    "username": "pecheur1",
    "isPremium": false,
    "captureCount": 0,
    "createdAt": "2026-04-20T10:00:00"
  }
}
```

**Erreurs possibles**
- `400` — email invalide ou mot de passe trop court
- `409` — email ou username déjà utilisé

---

### `POST /api/auth/login`

Connexion et récupération du JWT.

**Body**
```json
{
  "email": "user@fishdex.fr",
  "password": "motdepasse123"
}
```

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "user@fishdex.fr",
      "username": "pecheur1",
      "isPremium": false,
      "captureCount": 3,
      "createdAt": "2026-04-20T10:00:00"
    }
  }
}
```

**Erreurs possibles**
- `401` — email inconnu ou mot de passe incorrect

---

## 5. Profil utilisateur

### `GET /api/users/me`

Récupérer le profil de l'utilisateur connecté.

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@fishdex.fr",
    "username": "pecheur1",
    "isPremium": false,
    "captureCount": 12,
    "createdAt": "2026-04-20T10:00:00"
  }
}
```

---

### `PUT /api/users/me`

Modifier le username de l'utilisateur connecté.

**Body**
```json
{
  "username": "nouveau_pseudo"
}
```

**Réponse `200 OK`**
```json
{
  "success": true,
  "message": "Profil mis à jour",
  "data": { /* UserResponse */ }
}
```

**Erreurs possibles**
- `409` — username déjà utilisé

---

### `GET /api/users/me/stats`

Statistiques complètes de l'utilisateur connecté.

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "totalCaptures": 12,
    "totalWeight": 34.5,
    "biggestCatch": {
      "id": 7,
      "userId": 1,
      "username": "pecheur1",
      "speciesName": "Silure glane",
      "species": { /* SpeciesResponse ou null */ },
      "weight": 15.2,
      "length": 140.0,
      "photoUrl": "https://res.cloudinary.com/...",
      "latitude": null,
      "longitude": null,
      "note": null,
      "caughtAt": "2026-04-15T08:30:00",
      "createdAt": "2026-04-15T09:00:00"
    },
    "capturesBySpecies": {
      "Brochet": 4,
      "Carpe": 3,
      "Sandre": 5
    },
    "mostActiveMonth": "2026-04",
    "joinedGroupsCount": 2
  }
}
```

> `biggestCatch` est `null` si aucune capture.  
> `totalWeight` est `null` si aucune capture.  
> `mostActiveMonth` est `null` si aucune capture.

---

## 6. Captures

### `POST /api/captures`

Créer une nouvelle capture.

> **Règle freemium :** les utilisateurs non-premium sont limités à **50 captures**. Au-delà, l'API retourne `403`.

**Body**
```json
{
  "speciesName": "Brochet",
  "weight": 2.4,
  "length": 65.0,
  "speciesId": 1,
  "latitude": 48.8566,
  "longitude": 2.3522,
  "note": "Belle prise au leurre",
  "caughtAt": "2026-04-20T08:30:00"
}
```

| Champ | Type | Règles |
|-------|------|--------|
| `speciesName` | string | Obligatoire, max 100 caractères |
| `weight` | number | Obligatoire, entre 0.01 et 999.99 (kg) |
| `length` | number | Obligatoire, entre 0.1 et 9999.9 (cm) |
| `speciesId` | number | Optionnel — ID d'une espèce du catalogue |
| `latitude` | number | Optionnel, entre -90 et 90 |
| `longitude` | number | Optionnel, entre -180 et 180 |
| `note` | string | Optionnel, max 500 caractères |
| `caughtAt` | datetime | Obligatoire, format ISO 8601 |

**Réponse `201 Created`**
```json
{
  "success": true,
  "message": "Capture ajoutée avec succès",
  "data": {
    "id": 42,
    "userId": 1,
    "username": "pecheur1",
    "speciesName": "Brochet",
    "species": {
      "id": 1,
      "commonName": "Brochet",
      "latinName": "Esox lucius",
      "description": "Grand prédateur d'eau douce...",
      "imageUrl": null,
      "minWeightKg": 0.5,
      "maxWeightKg": 20.0,
      "habitat": "Eau douce"
    },
    "weight": 2.4,
    "length": 65.0,
    "photoUrl": null,
    "latitude": 48.8566,
    "longitude": 2.3522,
    "note": "Belle prise au leurre",
    "caughtAt": "2026-04-20T08:30:00",
    "createdAt": "2026-04-20T09:00:00"
  }
}
```

> `species` est `null` si `speciesId` n'est pas fourni.

**Erreurs possibles**
- `400` — champs obligatoires manquants ou invalides
- `403` — limite 50 captures freemium atteinte
- `404` — `speciesId` inexistant dans le catalogue

---

### `GET /api/captures?page=0&size=20`

Lister les captures de l'utilisateur connecté (triées par date de pêche décroissante).

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "content": [ /* CaptureResponse[] */ ],
    "totalElements": 12,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

### `GET /api/captures/{id}`

Récupérer une capture par son ID.

**Réponse `200 OK`** — `CaptureResponse`

**Erreurs possibles**
- `403` — cette capture n'appartient pas à l'utilisateur connecté
- `404` — capture introuvable

---

### `PUT /api/captures/{id}`

Modifier une capture existante.

**Body** — même structure que `POST /api/captures`

**Réponse `200 OK`** — `CaptureResponse` mis à jour

**Erreurs possibles**
- `403` — pas le propriétaire
- `404` — introuvable

---

### `DELETE /api/captures/{id}`

Supprimer une capture.

**Réponse `204 No Content`**

**Erreurs possibles**
- `403` — pas le propriétaire
- `404` — introuvable

---

## 7. Photos

### `POST /api/captures/{id}/photo`

Uploader ou remplacer la photo d'une capture.

**Content-Type :** `multipart/form-data`

| Champ | Type | Règles |
|-------|------|--------|
| `file` | File | Obligatoire, JPEG / PNG / WEBP, max 5 MB |

**Réponse `200 OK`** — `CaptureResponse` avec `photoUrl` remplie

**Erreurs possibles**
- `400` — format non supporté ou fichier trop lourd
- `403` — pas le propriétaire
- `404` — capture introuvable

---

### `DELETE /api/captures/{id}/photo`

Supprimer la photo d'une capture.

**Réponse `204 No Content`**

**Erreurs possibles**
- `403` — pas le propriétaire
- `404` — capture introuvable

---

## 8. Espèces

Catalogue de 20 espèces d'eau douce françaises, seedé au démarrage.

### `GET /api/species?page=0&size=20&search=brochet`

Lister les espèces avec recherche optionnelle par nom commun.

| Paramètre | Type | Description |
|-----------|------|-------------|
| `search` | string | Optionnel — filtre par nom commun (insensible à la casse) |

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "commonName": "Brochet",
        "latinName": "Esox lucius",
        "description": "Grand prédateur d'eau douce...",
        "imageUrl": null,
        "minWeightKg": 0.5,
        "maxWeightKg": 20.0,
        "habitat": "Eau douce"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

### `GET /api/species/{id}`

Récupérer une espèce par son ID.

**Réponse `200 OK`** — `SpeciesResponse`

**Erreurs possibles**
- `404` — espèce introuvable

---

## 9. Groupes

### `POST /api/groups`

Créer un groupe. Le créateur devient automatiquement **ADMIN** et premier membre.

**Body**
```json
{
  "name": "Les Brocheteurs du Nord",
  "description": "Club de pêche au brochet",
  "type": "CLUB"
}
```

| Champ | Type | Valeurs |
|-------|------|---------|
| `name` | string | Obligatoire, 3–100 caractères, unique |
| `description` | string | Optionnel, max 500 caractères |
| `type` | string | `CLUB` ou `ASSOCIATION` |

**Réponse `201 Created`**
```json
{
  "success": true,
  "message": "Groupe créé",
  "data": {
    "id": 5,
    "name": "Les Brocheteurs du Nord",
    "description": "Club de pêche au brochet",
    "type": "CLUB",
    "isPro": false,
    "memberCount": 1,
    "creatorUsername": "pecheur1",
    "createdAt": "2026-04-20T10:00:00"
  }
}
```

**Erreurs possibles**
- `409` — nom de groupe déjà utilisé

---

### `GET /api/groups/{id}`

Récupérer les détails d'un groupe.

**Réponse `200 OK`** — `GroupResponse`

**Erreurs possibles**
- `404` — groupe introuvable

---

### `POST /api/groups/{id}/join`

Rejoindre un groupe (rôle MEMBER).

**Réponse `200 OK`**
```json
{
  "success": true,
  "message": "Vous avez rejoint le groupe",
  "data": null
}
```

**Erreurs possibles**
- `404` — groupe introuvable
- `409` — déjà membre du groupe

---

### `GET /api/groups/{id}/feed?page=0&size=20`

Fil d'activité du groupe — captures de tous les membres, triées par date décroissante.

> Accessible aux **membres uniquement**.

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "captureId": 42,
        "userId": 1,
        "username": "pecheur1",
        "speciesName": "Brochet",
        "weight": 2.4,
        "length": 65.0,
        "photoUrl": "https://res.cloudinary.com/...",
        "caughtAt": "2026-04-20T08:30:00",
        "createdAt": "2026-04-20T09:00:00"
      }
    ],
    "totalElements": 7,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

**Erreurs possibles**
- `403` — vous n'êtes pas membre du groupe
- `404` — groupe introuvable

---

## 10. Badges

Les badges sont attribués **automatiquement** par le serveur lors des actions. Aucun appel spécifique n'est nécessaire pour les déclencher.

### `GET /api/badges/me`

Lister les badges obtenus par l'utilisateur connecté.

**Réponse `200 OK`**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "FIRST_CATCH",
      "label": "Première capture",
      "earnedAt": "2026-04-20T09:05:00"
    },
    {
      "id": 2,
      "type": "PHOTOGRAPHER",
      "label": "Photographe (3 photos)",
      "earnedAt": "2026-04-20T14:00:00"
    }
  ]
}
```

**Catalogue des badges**

| `type` | `label` | Déclencheur |
|--------|---------|-------------|
| `FIRST_CATCH` | Première capture | 1ère capture enregistrée |
| `TEN_CATCHES` | 10 captures | 10 captures enregistrées |
| `FIFTY_CATCHES` | 50 captures | 50 captures enregistrées |
| `FIRST_GROUP` | Premier groupe rejoint | Rejoindre un groupe |
| `SPECIES_COLLECTOR` | Collectionneur (5 espèces) | 5 espèces différentes pêchées |
| `PHOTOGRAPHER` | Photographe (3 photos) | 3 captures avec une photo |

---

## 11. Modèles de données

### UserResponse
```typescript
{
  id: number
  email: string
  username: string
  isPremium: boolean
  captureCount: number
  createdAt: string // ISO 8601
}
```

### CaptureResponse
```typescript
{
  id: number
  userId: number
  username: string
  speciesName: string
  species: SpeciesResponse | null
  weight: number         // kg
  length: number         // cm
  photoUrl: string | null
  latitude: number | null
  longitude: number | null
  note: string | null
  caughtAt: string       // ISO 8601
  createdAt: string      // ISO 8601
}
```

### SpeciesResponse
```typescript
{
  id: number
  commonName: string
  latinName: string | null
  description: string | null
  imageUrl: string | null
  minWeightKg: number | null
  maxWeightKg: number | null
  habitat: string | null
}
```

### GroupResponse
```typescript
{
  id: number
  name: string
  description: string | null
  type: "CLUB" | "ASSOCIATION"
  isPro: boolean
  memberCount: number
  creatorUsername: string
  createdAt: string // ISO 8601
}
```

### FeedItemResponse
```typescript
{
  captureId: number
  userId: number
  username: string
  speciesName: string
  weight: number
  length: number
  photoUrl: string | null
  caughtAt: string  // ISO 8601
  createdAt: string // ISO 8601
}
```

### BadgeResponse
```typescript
{
  id: number
  type: "FIRST_CATCH" | "TEN_CATCHES" | "FIFTY_CATCHES" | "FIRST_GROUP" | "SPECIES_COLLECTOR" | "PHOTOGRAPHER"
  label: string
  earnedAt: string // ISO 8601
}
```

### UserStatsResponse
```typescript
{
  totalCaptures: number
  totalWeight: number | null
  biggestCatch: CaptureResponse | null
  capturesBySpecies: Record<string, number>
  mostActiveMonth: string | null  // format "YYYY-MM"
  joinedGroupsCount: number
}
```

---

## Exemple de flux complet

```
1. POST /api/auth/register        → créer un compte
2. POST /api/auth/login           → récupérer le token → stocker en localStorage
3. GET  /api/users/me             → afficher le profil
4. GET  /api/species?search=bro   → autocomplete espèce dans le formulaire
5. POST /api/captures             → enregistrer une prise (avec speciesId optionnel)
6. POST /api/captures/{id}/photo  → uploader une photo (multipart)
7. GET  /api/captures             → afficher la liste de mes prises
8. GET  /api/users/me/stats       → dashboard de l'utilisateur
9. GET  /api/badges/me            → afficher les badges gagnés
10. POST /api/groups              → créer un club
11. POST /api/groups/{id}/join    → rejoindre un club (autre user)
12. GET  /api/groups/{id}/feed    → fil d'activité du club
```
