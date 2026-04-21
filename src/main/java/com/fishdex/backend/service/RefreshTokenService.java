package com.fishdex.backend.service;

import com.fishdex.backend.entity.RefreshToken;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    // 30 jours en ms pour remember-me
    private static final long REMEMBER_ME_MS = 30L * 24 * 60 * 60 * 1000;

    private final RefreshTokenRepository refreshTokenRepository;

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau refresh token (multi-session).
     * L'ancien token N'est PAS supprimé — plusieurs sessions simultanées autorisées.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, boolean rememberMe, String deviceInfo, String ipAddress) {
        long expirationMs = rememberMe ? REMEMBER_ME_MS : refreshExpirationMs;

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .rememberMe(rememberMe)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .lastUsedAt(LocalDateTime.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(token);
        log.debug("Refresh token créé pour {} (rememberMe={}, device={})", user.getEmail(), rememberMe, deviceInfo);
        return saved;
    }

    /** Compatibilité — crée un token sans informations de session */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, false, null, null);
    }

    // ── Validate ──────────────────────────────────────────────────────────

    @Transactional
    public RefreshToken validate(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(
                        "Refresh token invalide ou expiré. Veuillez vous reconnecter.",
                        HttpStatus.UNAUTHORIZED));

        if (!token.isValid()) {
            throw new BusinessException(
                    "Refresh token expiré ou révoqué. Veuillez vous reconnecter.",
                    HttpStatus.UNAUTHORIZED);
        }

        token.setLastUsedAt(LocalDateTime.now());
        return refreshTokenRepository.save(token);
    }

    // ── Revoke ────────────────────────────────────────────────────────────

    /** Révoque toutes les sessions de l'utilisateur (logout global / reset pwd) */
    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Toutes les sessions révoquées pour {}", user.getEmail());
    }

    /** Révoque une session spécifique (logout d'un appareil) */
    @Transactional
    public boolean revokeById(Long sessionId, User user) {
        boolean revoked = refreshTokenRepository.revokeByIdAndUser(sessionId, user) > 0;
        if (revoked) log.info("Session {} révoquée pour {}", sessionId, user.getEmail());
        return revoked;
    }

    // ── Query ─────────────────────────────────────────────────────────────

    public List<RefreshToken> getActiveSessions(User user) {
        return refreshTokenRepository.findByUserAndRevokedFalseOrderByCreatedAtDesc(user);
    }
}
