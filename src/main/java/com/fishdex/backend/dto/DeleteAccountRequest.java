package com.fishdex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RGPD Article 17 — Confirmation de suppression du compte.
 * Requiert le mot de passe pour éviter les suppressions accidentelles ou malveillantes.
 */
@Data
public class DeleteAccountRequest {

    @NotBlank(message = "Le mot de passe est requis pour confirmer la suppression")
    private String password;
}
