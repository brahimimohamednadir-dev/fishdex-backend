package com.fishdex.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.TotpSetupResponse;
import com.fishdex.backend.entity.TotpSecret;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.TotpSecretRepository;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TotpService {

    @Value("${spring.application.name:FishDex}")
    private String appName;

    private final TotpUtil totpUtil;
    private final TotpSecretRepository totpSecretRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ── Setup ─────────────────────────────────────────────────────────────

    /**
     * Génère un nouveau secret TOTP et des codes de secours.
     * N'active pas encore la 2FA — l'utilisateur doit confirmer avec un code valide.
     */
    @Transactional
    public TotpSetupResponse setupTotp(User user) {
        // Supprimer l'ancien secret si existant
        totpSecretRepository.findByUser(user).ifPresent(totpSecretRepository::delete);

        String secret = totpUtil.generateSecret();
        List<String> backupCodes = totpUtil.generateBackupCodes();

        // Hasher les codes de secours
        List<String> hashedCodes = backupCodes.stream()
                .map(passwordEncoder::encode)
                .toList();

        TotpSecret totpSecret = TotpSecret.builder()
                .user(user)
                .secret(secret)
                .backupCodes(toJson(hashedCodes))
                .enabled(false)
                .build();

        totpSecretRepository.save(totpSecret);

        String qrCodeUri = totpUtil.generateTotpUri(secret, user.getEmail(), appName);

        log.info("Setup TOTP initié pour {}", user.getEmail());
        return TotpSetupResponse.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .backupCodes(backupCodes) // codes en clair UNE SEULE fois
                .build();
    }

    /**
     * Active la 2FA après vérification du premier code TOTP.
     */
    @Transactional
    public void enableTotp(User user, String code) {
        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(
                        "Aucun setup TOTP en cours. Commencez par POST /api/2fa/setup", HttpStatus.BAD_REQUEST));

        if (totpSecret.getEnabled()) {
            throw new BusinessException("La 2FA est déjà activée", HttpStatus.CONFLICT);
        }

        if (!totpUtil.verifyTotp(totpSecret.getSecret(), code)) {
            throw new BusinessException("Code TOTP invalide", HttpStatus.UNAUTHORIZED);
        }

        totpSecret.setEnabled(true);
        totpSecret.setEnabledAt(LocalDateTime.now());
        totpSecretRepository.save(totpSecret);

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        log.info("2FA activée pour {}", user.getEmail());
    }

    /**
     * Désactive la 2FA — confirmation par mot de passe (comme le frontend l'envoie).
     */
    @Transactional
    public void disableTotp(User user, String password) {
        TotpSecret totpSecret = totpSecretRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("La 2FA n'est pas configurée", HttpStatus.BAD_REQUEST));

        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Mot de passe incorrect", HttpStatus.UNAUTHORIZED);
        }

        totpSecretRepository.delete(totpSecret);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        log.info("2FA désactivée pour {}", user.getEmail());
    }

    // ── Verification ──────────────────────────────────────────────────────

    /**
     * Vérifie un code TOTP ou un code de secours pour l'utilisateur.
     */
    public boolean verifyCode(User user, String code) {
        return totpSecretRepository.findByUser(user)
                .filter(TotpSecret::getEnabled)
                .map(ts -> totpUtil.verifyTotp(ts.getSecret(), code) || verifyBackupCode(ts, code, user))
                .orElse(false);
    }

    private boolean verifyBackupCode(TotpSecret totpSecret, String code, User user) {
        try {
            List<String> hashed = fromJson(totpSecret.getBackupCodes());
            String upperCode = code.toUpperCase().trim();
            for (int i = 0; i < hashed.size(); i++) {
                if (passwordEncoder.matches(upperCode, hashed.get(i))) {
                    // Invalider ce code de secours (usage unique)
                    hashed.remove(i);
                    totpSecret.setBackupCodes(toJson(hashed));
                    totpSecretRepository.save(totpSecret);
                    log.warn("Code de secours utilisé pour {}", user.getEmail());
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Erreur vérification code de secours", e);
        }
        return false;
    }

    // ── Status ────────────────────────────────────────────────────────────

    public boolean isTotpEnabled(User user) {
        return Boolean.TRUE.equals(user.getTwoFactorEnabled());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur sérialisation backup codes", e);
        }
    }

    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Erreur désérialisation backup codes", e);
        }
    }
}
