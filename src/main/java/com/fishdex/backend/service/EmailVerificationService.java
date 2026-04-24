package com.fishdex.backend.service;

import com.fishdex.backend.entity.EmailVerificationToken;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.EmailVerificationTokenRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /** Génère et envoie un email de vérification */
    @Transactional
    public void sendVerificationEmail(User user) {
        // Invalider les anciens tokens
        tokenRepository.invalidateAllByUser(user);

        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), tokenValue);
        log.info("Email de vérification envoyé à {}", user.getEmail());
    }

    /** Vérifie le token et marque l'email comme vérifié */
    @Transactional
    public void verifyEmail(String tokenValue) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Token de vérification invalide", HttpStatus.BAD_REQUEST));

        if (!token.isValid()) {
            throw new BusinessException(
                    "Ce lien de vérification a expiré ou a déjà été utilisé", HttpStatus.BAD_REQUEST);
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Email vérifié pour l'utilisateur {}", user.getEmail());
    }
}
