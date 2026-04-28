package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Statistiques personnelles enrichies — "Mon année en pêche".
 * GET /api/users/me/personal-stats
 */
@Data
@Builder
public class PersonalStatsResponse {

    // ── Chiffres clés ─────────────────────────────────────────────────────
    private long   totalCaptures;
    private long   thisYear;
    private long   thisMonth;
    private long   distinctSpecies;
    private Double heaviestCatchKg;
    private String heaviestCatchSpecies;
    private Double longestCatchCm;
    private String longestCatchSpecies;

    // ── Courbe mensuelle (12 mois de l'année en cours) ───────────────────
    /** { month: 1..12, label: "Jan", count: 3 } */
    private List<MonthStat> monthlyCaptures;

    // ── Palmarès espèces (top 10) ─────────────────────────────────────────
    /** { speciesName, count, recordWeight, recordLength } */
    private List<SpeciesRecord> topSpecies;

    // ── Spots favoris (top 5 — groupés par coordonnées arrondies) ─────────
    /** { lat, lng, count, label } */
    private List<SpotStat> favoriteSpots;

    // ── Inner DTOs ────────────────────────────────────────────────────────

    @Data @Builder
    public static class MonthStat {
        private int    month;   // 1–12
        private String label;   // "Jan", "Fév"...
        private long   count;
    }

    @Data @Builder
    public static class SpeciesRecord {
        private String speciesName;
        private long   count;
        private Double recordWeight;
        private Double recordLength;
    }

    @Data @Builder
    public static class SpotStat {
        private Double lat;
        private Double lng;
        private long   count;
        private String label; // adresse ou coordonnées tronquées
    }
}
