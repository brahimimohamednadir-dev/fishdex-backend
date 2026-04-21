package com.fishdex.backend.service;

import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gestion du verrouillage de compte après tentatives échouées.
 * Règle : 5 tentatives → compte bloqué 15 minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final EmailService emailService;

    /** Incrémente le compteur et verrouille si le seuil est atteint */
    @Transactional
    public void recordFailure(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            log.warn("Compte verrouillé pour {} après {} tentatives", user.getEmail(), attempts);
            emailService.sendAccountLockedAlert(user.getEmail(), user.getUsername(), LOCK_MINUTES);
        }

        userRepository.save(user);
    }

    /** Remet le compteur à zéro après une connexion réussie */
    @Transactional
    public void recordSuccess(User user) {
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}
