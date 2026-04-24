package com.fishdex.backend.service;

import com.fishdex.backend.entity.PasswordResetToken;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.PasswordResetTokenRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Initie la procédure de reset.
     * Anti-énumération : retourne toujours succès même si l'email n'existe pas.
     */
    @Transactional
    public void initiateReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) {
            log.debug("Reset demandé pour email inconnu : {} (ignoré silencieusement)", email);
            return; // anti-énumération
        }

        User user = userOpt.get();
        tokenRepository.invalidateAllByUser(user);

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(token);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), tokenValue);
        log.info("Reset de mot de passe initié pour {}", user.getEmail());
    }

    /** Applique le nouveau mot de passe et révoque toutes les sessions */
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Token invalide ou expiré", HttpStatus.BAD_REQUEST));

        if (!token.isValid()) {
            throw new BusinessException("Ce lien a expiré ou a déjà été utilisé", HttpStatus.BAD_REQUEST);
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Révoquer toutes les sessions existantes (sécurité)
        refreshTokenService.revokeByUser(user);

        emailService.sendPasswordChangedAlert(user.getEmail(), user.getUsername());
        log.info("Mot de passe réinitialisé pour {}", user.getEmail());
    }
}
