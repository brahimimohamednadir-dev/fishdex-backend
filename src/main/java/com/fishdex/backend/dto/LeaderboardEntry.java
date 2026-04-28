package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LeaderboardEntry {
    int rank;
    Long userId;
    String username;
    String userTag;
    long totalCaptures;
    double totalWeight;
    long distinctSpecies;
    double score; // valeur triée selon le type demandé
}
