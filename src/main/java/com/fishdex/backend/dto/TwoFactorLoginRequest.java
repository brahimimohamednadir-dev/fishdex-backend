package com.fishdex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorLoginRequest {

    /** Token intermédiaire reçu à l'étape 1 du login (nommé tempToken côté frontend) */
    @NotBlank(message = "Le tempToken est obligatoire")
    private String tempToken;

    /** Code TOTP à 6 chiffres (nommé code côté frontend) */
    @NotBlank(message = "Le code TOTP est obligatoire")
    private String code;

    private String deviceInfo;

    /** Si true → refresh token 30 jours */
    private Boolean rememberMe = false;

    /** Si true → ce device sera de confiance pour la 2FA pendant 30 jours */
    private Boolean trustDevice = false;
}
