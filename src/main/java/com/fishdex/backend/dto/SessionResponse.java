package com.fishdex.backend.dto;

import com.fishdex.backend.entity.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Correspond exactement à l'interface UserSession du frontend Angular :
 * { id, deviceInfo, ip, lastActive, trusted, current }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private String deviceInfo;

    /** Adresse IP — nommé "ip" pour correspondre au frontend (UserSession.ip) */
    private String ip;

    /** Dernière utilisation — nommé "lastActive" pour correspondre au frontend */
    private String lastActive;

    /** Trusting 2FA — nommé "trusted" pour correspondre au frontend */
    private Boolean trusted;

    /**
     * Indique si c'est la session courante.
     * Le backend ne peut pas le savoir directement ici — toujours false.
     * Le frontend peut le déduire en comparant avec le token actuel.
     */
    private Boolean current;

    public static SessionResponse from(RefreshToken token) {
        return SessionResponse.builder()
                .id(token.getId())
                .deviceInfo(token.getDeviceInfo())
                .ip(token.getIpAddress())
                .lastActive(token.getLastUsedAt() != null
                        ? token.getLastUsedAt().toString()
                        : token.getCreatedAt() != null ? token.getCreatedAt().toString() : null)
                .trusted(token.getTrustedForTwoFa())
                .current(false) // enrichi côté client si nécessaire
                .build();
    }
}
