package com.fishdex.backend.config;

import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * Migration one-shot : attribue un userTag unique à tous les comptes qui n'en ont pas.
 * S'exécute au démarrage après le seed espèces (Order 2).
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class UserTagMigration implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SecureRandom rng = new SecureRandom();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> usersWithoutTag = userRepository.findAll().stream()
                .filter(u -> u.getUserTag() == null || u.getUserTag().isBlank())
                .toList();

        if (usersWithoutTag.isEmpty()) return;

        log.info("Migration userTag : attribution de tags à {} compte(s)...", usersWithoutTag.size());

        for (User user : usersWithoutTag) {
            user.setUserTag(generateUniqueTag());
            userRepository.save(user);
        }

        log.info("Migration userTag terminée.");
    }

    private String generateUniqueTag() {
        for (int i = 0; i < 200; i++) {
            String tag = String.format("%05d", rng.nextInt(100_000));
            if (!userRepository.existsByUserTag(tag)) return tag;
        }
        throw new IllegalStateException("Pool de tags épuisé — impossible de générer un tag unique");
    }
}
