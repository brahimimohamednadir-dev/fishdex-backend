package com.fishdex.backend.service;

import com.fishdex.backend.dto.GdprExportResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RGPD — Articles 17 (droit à l'effacement) et 20 (portabilité des données).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GdprService {

    private final UserRepository              userRepository;
    private final CaptureRepository           captureRepository;
    private final RefreshTokenRepository      refreshTokenRepository;
    private final NotificationRepository      notificationRepository;
    private final FriendshipRepository        friendshipRepository;
    private final PostRepository              postRepository;
    private final PostCommentRepository       postCommentRepository;
    private final PostReactionRepository      postReactionRepository;
    private final CaptureCommentRepository    captureCommentRepository;
    private final CaptureReactionRepository   captureReactionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository     passwordResetTokenRepository;
    private final PasswordEncoder             passwordEncoder;

    // ── Article 20 : portabilité ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GdprExportResponse exportMyData(String email) {
        User user = loadUser(email);

        List<Capture> captures = captureRepository.findByUserOrderByCaughtAtDesc(user);
        List<GdprExportResponse.CaptureExport> captureExports = captures.stream()
                .map(c -> GdprExportResponse.CaptureExport.builder()
                        .id(c.getId())
                        .speciesName(c.getSpeciesName())
                        .weight(c.getWeight())
                        .length(c.getLength())
                        .latitude(c.getLatitude())
                        .longitude(c.getLongitude())
                        .notes(c.getNote())
                        .caughtAt(c.getCaughtAt())
                        .build())
                .toList();

        return GdprExportResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .userTag(user.getUserTag())
                .accountCreatedAt(user.getCreatedAt())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .captures(captureExports)
                .exportedAt(LocalDateTime.now())
                .notice("Données exportées conformément au RGPD Article 20. " +
                        "Pour exercer votre droit à l'effacement, utilisez DELETE /api/users/me.")
                .build();
    }

    // ── Article 17 : droit à l'effacement ────────────────────────────────────

    /**
     * Suppression complète du compte et de toutes les données associées.
     * Les captures, posts, commentaires et réactions sont définitivement supprimés.
     * Opération irréversible — requiert confirmation du mot de passe.
     */
    @Transactional
    public void deleteMyAccount(String email, String passwordConfirmation) {
        User user = loadUser(email);

        // Vérification du mot de passe obligatoire
        if (!passwordEncoder.matches(passwordConfirmation, user.getPassword())) {
            throw new BusinessException("Mot de passe incorrect", HttpStatus.FORBIDDEN);
        }

        log.info("[RGPD] Suppression du compte userId={} initiée", user.getId());

        // 1. Tokens de sécurité
        refreshTokenRepository.deleteAllByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);


        // 2. Activité sociale
        notificationRepository.deleteAllByRecipient(user);
        notificationRepository.deleteAllByActor(user);
        friendshipRepository.deleteAllByUserOrFriend(user, user);
        captureReactionRepository.deleteAllByUser(user);
        captureCommentRepository.deleteAllByUser(user);
        postReactionRepository.deleteAllByUser(user);
        postCommentRepository.deleteAllByUser(user);

        // 3. Posts et captures
        postRepository.deleteAllByUser(user);
        captureRepository.deleteAllByUser(user);

        // 4. Compte utilisateur
        userRepository.delete(user);

        log.info("[RGPD] Compte userId={} supprimé définitivement", user.getId());
    }

    private User loadUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));
    }
}
