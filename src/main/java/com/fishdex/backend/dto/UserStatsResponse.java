package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Réponse pour GET /api/users/me/stats
 * Correspond exactement à l'interface UserStats du frontend Angular.
 */
@Data
@Builder
public class UserStatsResponse {

    /** Nombre total de captures */
    private long totalCaptures;

    /** Poids total de toutes les captures en kg */
    private double totalWeight;

    /** La plus grosse capture (null si aucune capture) */
    private CaptureResponse biggestCatch;

    /** Captures par nom d'espèce : { "Brochet": 3, "Carpe": 5, ... } */
    private Map<String, Long> capturesBySpecies;

    /** Mois le plus actif au format "2024-06" (null si aucune capture) */
    private String mostActiveMonth;

    /** Nombre de groupes rejoints */
    private long joinedGroupsCount;
}
