package com.fishdex.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.entity.Species;
import com.fishdex.backend.entity.SpeciesTip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Répond exactement à l'interface Species du frontend Angular.
 * Les champs complexes (monthlyActivity, hourlyActivity, baits, techniques, equipment)
 * renvoient des tableaux vides en v0 — le frontend affiche des états "empty".
 */
@Data
@Builder
public class SpeciesResponse {

    // ── Identité ──────────────────────────────────────────────────────────
    private Long id;
    private String commonName;
    private String latinName;
    private String family;
    private String description;
    private String imageUrl;

    // ── Dimensions ────────────────────────────────────────────────────────
    private Double minWeightKg;
    private Double maxWeightKg;
    private Double minLengthCm;
    private Double maxLengthCm;

    // ── Classification ────────────────────────────────────────────────────
    /** Ex : ["FRESHWATER"] ou ["FRESHWATER", "BRACKISH"] */
    private List<String> waterTypes;
    private String difficulty;
    private String conservationStatus;

    // ── Habitat ───────────────────────────────────────────────────────────
    private String habitat;
    private String habitatDetail;
    private String preferredDepth;
    private String temperature;

    // ── Calendrier & activité ─────────────────────────────────────────────
    @Builder.Default private List<MonthlyActivityDto> monthlyActivity = Collections.emptyList();
    @Builder.Default private List<HourlyActivityDto>  hourlyActivity  = Collections.emptyList();

    // ── Techniques & matériel ─────────────────────────────────────────────
    @Builder.Default private List<BaitDto>      baits      = Collections.emptyList();
    @Builder.Default private List<TechniqueDto> techniques = Collections.emptyList();
    @Builder.Default private List<EquipmentDto> equipment  = Collections.emptyList();

    // ── Communauté ────────────────────────────────────────────────────────
    private List<CommunityTipDto> communityTips;
    private long totalCaptures;
    private SpeciesRecordDto fishDexRecord;

    // ── Auth-dépendant ────────────────────────────────────────────────────
    private boolean isCaught;
    private SpeciesPersonalStatsDto personalStats;

    // ── DTOs imbriqués ────────────────────────────────────────────────────

    @Data @Builder
    public static class CommunityTipDto {
        private Long id;
        private String content;
        private String authorUsername;
        private int upvotes;
        private boolean hasUpvoted;
        private String createdAt;
    }

    @Data @Builder
    public static class SpeciesRecordDto {
        private double weight;
        private Double length;
        private String username;
        private String date;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyActivityDto {
        private int month;
        private String status;
        private boolean legalClosure;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HourlyActivityDto {
        private int hour;
        private int activityLevel;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BaitDto {
        private Long id;
        private String name;
        private String type;
        private int effectiveness;
        private List<String> seasons;
        private String conditions;
        private String imageUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TechniqueDto {
        private Long id;
        private String name;
        private String description;
        private String difficulty;
        private List<String> bestSeasons;
        private String proTip;
        private String commonMistake;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EquipmentDto {
        private String name;
        private String description;
        private String budget;
        @JsonProperty("essential")
        private boolean essential;
    }

    @Data @Builder
    public static class SpeciesPersonalStatsDto {
        private long totalCatches;
        private PersonalRecordDto personalRecord;
        private Double averageWeight;
        private String lastCatch;
        private long caughtThisYear;
    }

    @Data @Builder
    public static class PersonalRecordDto {
        private Double weight;
        private Double length;
        private String date;
    }

    // ── Jackson helper ────────────────────────────────────────────────────

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static <T> List<T> parseJson(String json, TypeReference<List<T>> ref) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try { return MAPPER.readValue(json, ref); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    // ── Factory — liste (sans données user) ──────────────────────────────

    public static SpeciesResponse from(Species species) {
        return fromWithContext(species, null, Collections.emptyList(), false, null, null, 0L);
    }

    // ── Factory — détail (avec données user + tips) ───────────────────────

    public static SpeciesResponse fromWithContext(
            Species species,
            String currentUserEmail,
            List<SpeciesTip> tips,
            boolean caught,
            SpeciesPersonalStatsDto personalStats,
            SpeciesRecordDto fishDexRecord,
            long totalCaptures) {

        List<String> waterTypeList = parseWaterTypes(species.getWaterTypes());

        List<CommunityTipDto> tipDtos = tips.stream()
                .map(t -> CommunityTipDto.builder()
                        .id(t.getId())
                        .content(t.getContent())
                        .authorUsername(t.getUser().getUsername())
                        .upvotes(t.getUpvotes())
                        .hasUpvoted(false) // enrichi dans SpeciesService quand user connecté
                        .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return SpeciesResponse.builder()
                .id(species.getId())
                .commonName(species.getCommonName())
                .latinName(species.getLatinName())
                .family(species.getFamily())
                .description(species.getDescription())
                .imageUrl(species.getImageUrl())
                .minWeightKg(species.getMinWeightKg())
                .maxWeightKg(species.getMaxWeightKg())
                .minLengthCm(species.getMinLengthCm())
                .maxLengthCm(species.getMaxLengthCm())
                .waterTypes(waterTypeList)
                .difficulty(species.getDifficulty())
                .conservationStatus(species.getConservationStatus())
                .habitat(species.getHabitat())
                .habitatDetail(species.getHabitatDetail())
                .preferredDepth(species.getPreferredDepth())
                .temperature(species.getTemperature())
                .monthlyActivity(parseJson(species.getMonthlyActivityJson(), new TypeReference<List<MonthlyActivityDto>>() {}))
                .hourlyActivity(parseJson(species.getHourlyActivityJson(),   new TypeReference<List<HourlyActivityDto>>()   {}))
                .baits(parseJson(species.getBaitsJson(),           new TypeReference<List<BaitDto>>()          {}))
                .techniques(parseJson(species.getTechniquesJson(), new TypeReference<List<TechniqueDto>>()     {}))
                .equipment(parseJson(species.getEquipmentJson(),   new TypeReference<List<EquipmentDto>>()     {}))
                .communityTips(tipDtos)
                .totalCaptures(totalCaptures)
                .fishDexRecord(fishDexRecord)
                .isCaught(caught)
                .personalStats(personalStats)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static List<String> parseWaterTypes(String raw) {
        if (raw == null || raw.isBlank()) return List.of("FRESHWATER");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** Sérialisation JSON : "isCaught" (camelCase exact du frontend) */
    public boolean isIsCaught() { return isCaught; }
}
