package com.fishdex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** Access token JWT (courte durée : 15 min en prod) */
    private String token;

    /** Toujours "Bearer" */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Durée de vie de l'access token en secondes */
    private long expiresIn;

    /** Refresh token opaque (longue durée : 7 ou 30 jours) */
    private String refreshToken;

    private UserResponse user;

    /**
     * Si true → l'utilisateur a la 2FA activée et le login est suspendu.
     * Le client doit envoyer le code TOTP via POST /api/auth/2fa/verify
     * avec le preAuthToken fourni.
     */
    @Builder.Default
    private Boolean requiresTwoFactor = false;

    /**
     * Token intermédiaire valable 5 minutes, fourni uniquement quand
     * requiresTwoFactor = true. Null dans tous les autres cas.
     * Nommé "tempToken" pour correspondre au frontend Angular.
     */
    private String tempToken;
}
