package com.fishdex.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CaptureRequest {

    /**
     * Nom libre de l'espèce (obligatoire).
     * Si speciesId est fourni, ce champ est auto-rempli depuis le catalogue si laissé vide.
     */
    @Size(max = 100, message = "Le nom de l'espèce ne doit pas dépasser 100 caractères")
    private String speciesName;

    /**
     * Lien optionnel vers le catalogue des espèces.
     * Si fourni et speciesName est vide, speciesName est déduit du catalogue.
     */
    private Long speciesId;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.01", message = "Le poids doit être supérieur à 0")
    @DecimalMax(value = "999.99", message = "Le poids ne peut pas dépasser 999.99 kg")
    private Double weight;

    @NotNull(message = "La longueur est obligatoire")
    @DecimalMin(value = "0.1", message = "La longueur doit être supérieure à 0")
    @DecimalMax(value = "9999.9", message = "La longueur ne peut pas dépasser 9999.9 cm")
    private Double length;

    private String photoUrl;

    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    @Size(max = 500, message = "La note ne doit pas dépasser 500 caractères")
    private String note;

    @NotNull(message = "La date de capture est obligatoire")
    @PastOrPresent(message = "La date de capture ne peut pas être dans le futur")
    private LocalDateTime caughtAt;

    /**
     * Visibilité : PUBLIC (défaut), FRIENDS, PRIVATE
     * Non obligatoire — défaut côté entité : PUBLIC
     */
    private String visibility;
}
