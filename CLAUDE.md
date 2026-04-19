# FishDex Backend

## Stack
- Spring Boot 3.5.13 + Java 21
- Spring Security + JWT (jjwt)
- Spring Data JPA + Hibernate
- MySQL 8
- Lombok + Validation
- Maven

## Architecture des packages
- controller/ → endpoints REST
- service/    → logique métier
- repository/ → requêtes JPA
- entity/     → entités MySQL
- dto/        → objets request/response
- config/     → SecurityConfig, CorsConfig
- security/   → JwtFilter, JwtService
- exception/  → GlobalExceptionHandler

## Conventions
- Toujours utiliser des DTOs, jamais exposer les entités directement
- Réponses standardisées via ApiResponse<T>
- Toutes les routes protégées sauf /api/auth/**
- snake_case pour les colonnes MySQL
- camelCase pour les variables Java

## Règles métier
- Limite freemium : 50 captures max sans abonnement premium
- Prix abonnement : 3€/mois
- Groupes Pro : 10€/mois pour clubs et associations

## Endpoints prévus
POST   /api/auth/register
POST   /api/auth/login
GET    /api/captures
POST   /api/captures
GET    /api/captures/{id}
PUT    /api/captures/{id}
DELETE /api/captures/{id}
GET    /api/species
GET    /api/groups/{id}/feed