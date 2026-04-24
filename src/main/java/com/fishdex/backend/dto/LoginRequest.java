package com.fishdex.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    /** Si true → refresh token de 30 jours au lieu de 7 */
    private Boolean rememberMe = false;

    /** User-Agent ou nom d'appareil (optionnel, pour l'affichage des sessions) */
    private String deviceInfo;
}
