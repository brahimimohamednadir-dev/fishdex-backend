package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "species")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false, length = 100)
    private String commonName;

    @Column(name = "latin_name", nullable = false, length = 100)
    private String latinName;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    /** Famille taxonomique (ex: Salmonidae) */
    @Column(length = 100)
    private String family;

    // ── Dimensions ────────────────────────────────────────────────────────

    @Column(name = "min_weight_kg")
    private Double minWeightKg;

    @Column(name = "max_weight_kg")
    private Double maxWeightKg;

    @Column(name = "min_length_cm")
    private Double minLengthCm;

    @Column(name = "max_length_cm")
    private Double maxLengthCm;

    /** Taille légale minimale de capture en cm */
    @Column(name = "min_legal_size")
    private Integer minLegalSize;

    // ── Classification ────────────────────────────────────────────────────

    /**
     * Types d'eau — valeurs séparées par des virgules.
     * Ex : "FRESHWATER" ou "FRESHWATER,BRACKISH"
     * Valeurs valides : FRESHWATER, SALTWATER, BRACKISH
     */
    @Column(name = "water_types", length = 100)
    private String waterTypes;

    /**
     * Niveau de difficulté : BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
     */
    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "conservation_status", length = 100)
    private String conservationStatus;

    // ── Habitat ───────────────────────────────────────────────────────────

    @Column(name = "habitat", length = 200)
    private String habitat;

    @Column(name = "habitat_detail", length = 500)
    private String habitatDetail;

    @Column(name = "preferred_depth", length = 100)
    private String preferredDepth;

    @Column(name = "temperature", length = 100)
    private String temperature;
}
