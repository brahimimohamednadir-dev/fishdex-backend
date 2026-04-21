package com.fishdex.backend.service;

import com.fishdex.backend.entity.Badge;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final CaptureRepository captureRepository;

    /**
     * Vérifie et attribue les badges liés aux captures pour l'utilisateur donné.
     */
    @Transactional
    public void checkAndAwardBadges(User user) {
        long captureCount = captureRepository.countByUserId(user.getId());

        if (captureCount >= 1)  award(user, Badge.BadgeType.FIRST_CAPTURE);
        if (captureCount >= 5)  award(user, Badge.BadgeType.CAPTURE_5);
        if (captureCount >= 10) award(user, Badge.BadgeType.CAPTURE_10);
    }

    /**
     * Attribue le badge "premier groupe" (à appeler lors de l'adhésion à un groupe).
     */
    @Transactional
    public void awardFirstGroup(User user) {
        award(user, Badge.BadgeType.FIRST_GROUP);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void award(User user, Badge.BadgeType type) {
        if (!badgeRepository.existsByUserIdAndType(user.getId(), type)) {
            badgeRepository.save(Badge.builder()
                    .user(user)
                    .type(type)
                    .awardedAt(LocalDateTime.now())
                    .build());
        }
    }
}
