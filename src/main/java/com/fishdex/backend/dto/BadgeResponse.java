package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Badge;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class BadgeResponse {

    private Long id;
    private String type;
    private String label;
    private String description;

    /** Nommé "earnedAt" pour correspondre au frontend Angular (Badge.earnedAt) */
    private LocalDateTime earnedAt;

    private static final Map<Badge.BadgeType, String> LABELS = Map.of(
            Badge.BadgeType.FIRST_CAPTURE, "Première prise",
            Badge.BadgeType.CAPTURE_5,     "Pêcheur confirmé",
            Badge.BadgeType.CAPTURE_10,    "Expert de la canne",
            Badge.BadgeType.SPECIES_3,     "Diversité aquatique",
            Badge.BadgeType.SPECIES_5,     "Chasseur de trophées",
            Badge.BadgeType.FIRST_GROUP,   "Esprit d'équipe"
    );

    private static final Map<Badge.BadgeType, String> DESCRIPTIONS = Map.of(
            Badge.BadgeType.FIRST_CAPTURE, "Vous avez enregistré votre toute première capture !",
            Badge.BadgeType.CAPTURE_5,     "5 captures au compteur — vous prenez le rythme.",
            Badge.BadgeType.CAPTURE_10,    "10 captures ! Vous maîtrisez votre spot.",
            Badge.BadgeType.SPECIES_3,     "3 espèces différentes capturées — belle diversité !",
            Badge.BadgeType.SPECIES_5,     "5 espèces différentes — un vrai naturaliste.",
            Badge.BadgeType.FIRST_GROUP,   "Vous avez rejoint votre premier groupe de pêche."
    );

    public static BadgeResponse from(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .type(badge.getType().name())
                .label(LABELS.getOrDefault(badge.getType(), badge.getType().name()))
                .description(DESCRIPTIONS.getOrDefault(badge.getType(), ""))
                .earnedAt(badge.getAwardedAt())
                .build();
    }
}
