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

    // ── Champs alignés sur le modèle frontend ────────────────────────────

    /** Poids minimum typique en kg (null si inconnu) */
    @Column(name = "min_weight_kg")
    private Double minWeightKg;

    /** Poids maximum typique en kg (null si inconnu) */
    @Column(name = "max_weight_kg")
    private Double maxWeightKg;

    /** Habitat principal (ex : "Rivières rapides", "Étangs et lacs calmes") */
    @Column(name = "habitat", length = 200)
    private String habitat;

    // ── Champs supplémentaires conservés ─────────────────────────────────

    /** Taille légale minimale de capture en cm */
    @Column(name = "min_legal_size")
    private Integer minLegalSize;

    /** Famille taxonomique */
    @Column(length = 100)
    private String family;
}
