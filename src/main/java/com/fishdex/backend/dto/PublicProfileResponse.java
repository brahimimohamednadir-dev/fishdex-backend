package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Profil public d'un utilisateur — visible par tous sans authentification.
 * GET /api/users/{username}
 */
@Data
@Builder
public class PublicProfileResponse {

    private Long   userId;
    private String username;
    private LocalDateTime memberSince;

    // ── Stats publiques ───────────────────────────────────────────────────
    private long   totalCaptures;
    private long   distinctSpecies;
    private Double heaviestCatchKg;
    private String heaviestCatchSpecies;

    // ── Dernières captures publiques (max 9 — grille 3×3) ────────────────
    private List<CaptureResponse> recentCaptures;

    // ── Badges débloqués ──────────────────────────────────────────────────
    private List<BadgeResponse> badges;

    // ── Relation avec le visiteur (null si anonyme) ───────────────────────
    /** ACCEPTED | PENDING_SENT | PENDING_RECEIVED | NONE | null */
    private String friendshipStatus;
    private Long   friendshipId;
}
