package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaptureStatsResponse {

    private long totalCaptures;
    private long distinctSpecies;          // espèces distinctes via le catalogue
    private Double heaviestCatchKg;        // poids record
    private Double longestCatchCm;         // longueur record
    private String mostCaughtSpeciesName;  // espèce la plus souvent pêchée (nom libre)
    private LocalDateTime firstCaptureDate;
    private LocalDateTime lastCaptureDate;
    private long capturesThisMonth;
    private long capturesThisYear;
}
