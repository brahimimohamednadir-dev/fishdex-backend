package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Species;
import com.fishdex.backend.entity.SpeciesTip;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Correspond à l'interface Species du frontend Angular.
 * Les champs absents de l'entité sont retournés null/vide —
 * le frontend les gère gracieusement avec des valeurs par défaut.
 */
@Data
@Builder
public class SpeciesResponse {

    private Long id;
    private String commonName;
    private String latinName;
    private String family;
    private String description;
    private String imageUrl;

    // Physical
    private Double minWeightKg;
    private Double maxWeightKg;
    private Double minLengthCm;
    private Double maxLengthCm;

    // Habitat
    private String habitat;

    // Community tips (peuplés uniquement dans le détail)
    private List<CommunityTipDto> communityTips;

    // Listes vides — le frontend les gère avec @if / optional chaining
    private List<Object> baits;
    private List<Object> techniques;
    private List<Object> equipment;
    private List<Object> monthlyActivity;
    private List<Object> hourlyActivity;

    // Stats dynamiques — null par défaut (non implémentées)
    private Long totalCaptures;
    private Object fishDexRecord;
    private Boolean isCaught;
    private Object personalStats;

    // ── Inner DTO ─────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class CommunityTipDto {
        private Long id;
        private String content;
        private String authorUsername;
        private int upvotes;
        private boolean hasUpvoted;
        private LocalDateTime createdAt;

        public static CommunityTipDto from(SpeciesTip tip, boolean hasUpvoted) {
            return CommunityTipDto.builder()
                    .id(tip.getId())
                    .content(tip.getContent())
                    .authorUsername(tip.getUser().getUsername())
                    .upvotes(tip.getUpvoteCount())
                    .hasUpvoted(hasUpvoted)
                    .createdAt(tip.getCreatedAt())
                    .build();
        }
    }

    // ── Factory methods ───────────────────────────────────────────────────

    /** Pour la liste (sans tips) */
    public static SpeciesResponse from(Species species) {
        return builder()
                .id(species.getId())
                .commonName(species.getCommonName())
                .latinName(species.getLatinName())
                .family(species.getFamily())
                .description(species.getDescription())
                .imageUrl(species.getImageUrl())
                .minWeightKg(species.getMinWeightKg())
                .maxWeightKg(species.getMaxWeightKg())
                .habitat(species.getHabitat())
                .communityTips(List.of())
                .baits(List.of())
                .techniques(List.of())
                .equipment(List.of())
                .monthlyActivity(List.of())
                .hourlyActivity(List.of())
                .build();
    }

    /** Pour le détail (avec tips) */
    public static SpeciesResponse from(Species species, List<CommunityTipDto> tips) {
        SpeciesResponse r = from(species);
        r.setCommunityTips(tips);
        return r;
    }
}
