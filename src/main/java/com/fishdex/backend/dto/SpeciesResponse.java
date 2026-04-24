package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Species;
import lombok.Builder;
import lombok.Data;

/**
 * Correspond exactement à l'interface Species du frontend Angular :
 * { id, commonName, latinName, description, imageUrl, minWeightKg, maxWeightKg, habitat }
 */
@Data
@Builder
public class SpeciesResponse {

    private Long id;
    private String commonName;
    private String latinName;
    private String description;
    private String imageUrl;

    /** Poids minimum typique en kg (correspond à Species.minWeightKg frontend) */
    private Double minWeightKg;

    /** Poids maximum typique en kg (correspond à Species.maxWeightKg frontend) */
    private Double maxWeightKg;

    /** Habitat principal (correspond à Species.habitat frontend) */
    private String habitat;

    public static SpeciesResponse from(Species species) {
        return SpeciesResponse.builder()
                .id(species.getId())
                .commonName(species.getCommonName())
                .latinName(species.getLatinName())
                .description(species.getDescription())
                .imageUrl(species.getImageUrl())
                .minWeightKg(species.getMinWeightKg())
                .maxWeightKg(species.getMaxWeightKg())
                .habitat(species.getHabitat())
                .build();
    }
}
