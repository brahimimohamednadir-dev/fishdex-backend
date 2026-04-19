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

    @Column(name = "common_name", nullable = false, unique = true, length = 100)
    private String commonName;

    @Column(name = "latin_name", length = 150)
    private String latinName;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "min_weight_kg")
    private Double minWeightKg;

    @Column(name = "max_weight_kg")
    private Double maxWeightKg;

    @Column(length = 100)
    private String habitat;
}
