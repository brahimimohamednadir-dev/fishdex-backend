package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Correspond exactement à l'interface Capture du frontend Angular :
 * { id, userId, username, speciesName, weight, length, photoUrl,
 *   latitude, longitude, note, caughtAt, createdAt, species }
 */
@Data
@Builder
public class CaptureResponse {

    private Long id;
    private Long userId;
    private String username;

    /** Nom libre saisi par l'utilisateur */
    private String speciesName;

    private Double weight;
    private Double length;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String note;
    private String visibility;
    private LocalDateTime caughtAt;
    private LocalDateTime createdAt;

    /**
     * Espèce liée au catalogue (null si non répertoriée).
     * Correspond au champ "species: Species | null" du frontend.
     */
    private SpeciesResponse species;

    // ── Météo ─────────────────────────────────────────────────────────────
    private Double  weatherTemp;
    private Double  weatherWind;
    private Double  weatherPressure;
    private Integer weatherClouds;
    private String  weatherDesc;
    private String  weatherIcon;

    public static CaptureResponse from(Capture capture) {
        return CaptureResponse.builder()
                .id(capture.getId())
                .userId(capture.getUser().getId())
                .username(capture.getUser().getUsername())
                .speciesName(capture.getSpeciesName())
                .weight(capture.getWeight())
                .length(capture.getLength())
                .photoUrl(capture.getPhotoUrl())
                .latitude(capture.getLatitude())
                .longitude(capture.getLongitude())
                .note(capture.getNote())
                .visibility(capture.getVisibility() != null ? capture.getVisibility().name() : "PUBLIC")
                .caughtAt(capture.getCaughtAt())
                .createdAt(capture.getCreatedAt())
                .species(capture.getSpecies() != null
                        ? SpeciesResponse.from(capture.getSpecies())
                        : null)
                .weatherTemp(capture.getWeatherTemp())
                .weatherWind(capture.getWeatherWind())
                .weatherPressure(capture.getWeatherPressure())
                .weatherClouds(capture.getWeatherClouds())
                .weatherDesc(capture.getWeatherDesc())
                .weatherIcon(capture.getWeatherIcon())
                .build();
    }
}
