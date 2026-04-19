package com.fishdex.backend.service;

import com.fishdex.backend.dto.BadgeResponse;
import com.fishdex.backend.entity.Badge;
import com.fishdex.backend.entity.Badge.BadgeType;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final CaptureRepository captureRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkAndAwardBadges(User user) {
        long count = captureRepository.countByUserId(user.getId());
        award(user, BadgeType.FIRST_CATCH, count >= 1);
        award(user, BadgeType.TEN_CATCHES, count >= 10);
        award(user, BadgeType.FIFTY_CATCHES, count >= 50);

        long distinctSpecies = captureRepository.findByUserId(user.getId())
                .stream()
                .map(Capture::getSpeciesName)
                .distinct()
                .count();
        award(user, BadgeType.SPECIES_COLLECTOR, distinctSpecies >= 5);

        long photosCount = captureRepository.countByUserIdAndPhotoUrlIsNotNull(user.getId());
        award(user, BadgeType.PHOTOGRAPHER, photosCount >= 3);
    }

    @Transactional
    public void awardFirstGroup(User user) {
        award(user, BadgeType.FIRST_GROUP, true);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getMyBadges(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(user.getId())
                .stream()
                .map(BadgeResponse::from)
                .toList();
    }

    private void award(User user, BadgeType type, boolean condition) {
        if (condition && !badgeRepository.existsByUserIdAndType(user.getId(), type)) {
            badgeRepository.save(Badge.builder().user(user).type(type).build());
        }
    }
}
