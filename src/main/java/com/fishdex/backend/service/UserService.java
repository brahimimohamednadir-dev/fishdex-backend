package com.fishdex.backend.service;

import com.fishdex.backend.dto.*;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final CaptureRepository captureRepository;
    private final GroupMemberRepository groupMemberRepository;

    // ── Chargement ────────────────────────────────────────────────────────

    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur non trouvé", HttpStatus.NOT_FOUND));
    }

    // ── Profil ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return UserResponse.from(loadUserByEmail(email));
    }

    @Transactional
    public UserResponse updateUsername(String email, UpdateUsernameRequest request) {
        User user = loadUserByEmail(email);
        String newUsername = request.getUsername().trim();

        if (!user.getUsername().equals(newUsername) &&
                userRepository.existsByUsername(newUsername)) {
            throw new BusinessException(
                    "Ce nom d'utilisateur est déjà pris", HttpStatus.CONFLICT);
        }

        user.setUsername(newUsername);
        return UserResponse.from(userRepository.save(user));
    }

    // ── Badges ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BadgeResponse> getMyBadges(String email) {
        User user = loadUserByEmail(email);
        return badgeRepository.findByUserId(user.getId())
                .stream()
                .map(BadgeResponse::from)
                .toList();
    }

    // ── Statistiques (format frontend) ────────────────────────────────────

    /**
     * GET /api/users/me/stats
     * Retourne UserStatsResponse qui correspond exactement au modèle UserStats du frontend.
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getMyStats(String email) {
        User user = loadUserByEmail(email);
        Long userId = user.getId();

        long totalCaptures = captureRepository.countByUserId(userId);

        // Poids total
        Double totalWeight = captureRepository.findTotalWeightByUserId(userId);

        // La plus grosse capture
        CaptureResponse biggestCatch = captureRepository.findHeaviestByUserId(userId)
                .map(CaptureResponse::from)
                .orElse(null);

        // Répartition par espèce
        Map<String, Long> capturesBySpecies = new LinkedHashMap<>();
        captureRepository.findCapturesBySpeciesNameForUser(userId).forEach(row ->
                capturesBySpecies.put((String) row[0], (Long) row[1])
        );

        // Mois le plus actif (format "YYYY-MM")
        String mostActiveMonth = null;
        List<Object[]> monthRows = captureRepository
                .findCapturesByMonthForUser(userId, PageRequest.of(0, 1));
        if (!monthRows.isEmpty()) {
            Object[] row = monthRows.get(0);
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            mostActiveMonth = String.format("%04d-%02d", year, month);
        }

        // Nombre de groupes rejoints
        long joinedGroupsCount = groupMemberRepository.countByUserId(userId);

        return UserStatsResponse.builder()
                .totalCaptures(totalCaptures)
                .totalWeight(totalWeight != null ? totalWeight : 0.0)
                .biggestCatch(biggestCatch)
                .capturesBySpecies(capturesBySpecies)
                .mostActiveMonth(mostActiveMonth)
                .joinedGroupsCount(joinedGroupsCount)
                .build();
    }

    // ── Statistiques de captures (endpoint legacy) ────────────────────────

    @Transactional(readOnly = true)
    public CaptureStatsResponse getMyCaptureStats(String email) {
        User user = loadUserByEmail(email);
        Long userId = user.getId();

        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime startOfYear = java.time.LocalDateTime.now()
                .withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        String mostCaught = null;
        List<Object[]> topSpecies = captureRepository
                .findMostCaughtSpeciesByUserId(userId, PageRequest.of(0, 1));
        if (!topSpecies.isEmpty()) {
            mostCaught = (String) topSpecies.get(0)[0];
        }

        return CaptureStatsResponse.builder()
                .totalCaptures(captureRepository.countByUserId(userId))
                .distinctSpecies(captureRepository.countDistinctSpeciesByUserId(userId))
                .heaviestCatchKg(captureRepository.findMaxWeightByUserId(userId))
                .longestCatchCm(captureRepository.findMaxLengthByUserId(userId))
                .mostCaughtSpeciesName(mostCaught)
                .firstCaptureDate(captureRepository.findFirstCaptureDateByUserId(userId))
                .lastCaptureDate(captureRepository.findLastCaptureDateByUserId(userId))
                .capturesThisMonth(captureRepository
                        .countByUserIdAndCaughtAtAfter(userId, startOfMonth))
                .capturesThisYear(captureRepository
                        .countByUserIdAndCaughtAtAfter(userId, startOfYear))
                .build();
    }
}
