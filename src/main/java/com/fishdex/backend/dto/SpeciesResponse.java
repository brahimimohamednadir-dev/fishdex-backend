package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Species;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpeciesResponse {

    private Long id;
    private String commonName;
    private String latinName;
    private String description;
    private String imageUrl;
    private Double minWeightKg;
    private Double maxWeightKg;
    private String habitat;

    public static SpeciesResponse from(Species s) {
        return SpeciesResponse.builder()
                .id(s.getId())
                .commonName(s.getCommonName())
                .latinName(s.getLatinName())
                .description(s.getDescription())
                .imageUrl(s.getImageUrl())
                .minWeightKg(s.getMinWeightKg())
                .maxWeightKg(s.getMaxWeightKg())
                .habitat(s.getHabitat())
                .build();
    }
}
