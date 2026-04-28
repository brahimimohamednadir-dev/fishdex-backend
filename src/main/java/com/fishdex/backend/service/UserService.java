package com.fishdex.backend.service;

import com.fishdex.backend.dto.*;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.Friendship;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.FriendshipRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final CaptureRepository captureRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FriendshipRepository friendshipRepository;

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

    // ── Profil public ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username, String currentUserEmail) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Profil introuvable", HttpStatus.NOT_FOUND));
        Long targetId = target.getId();

        long totalCaptures    = captureRepository.countByUserId(targetId);
        long distinctSpecies  = captureRepository.countDistinctSpeciesByUserId(targetId);

        // Capture la plus lourde
        Double heaviestKg = null;
        String heaviestSpecies = null;
        Optional<Capture> heaviest = captureRepository.findHeaviestByUserId(targetId);
        if (heaviest.isPresent()) {
            heaviestKg      = heaviest.get().getWeight();
            heaviestSpecies = heaviest.get().getSpeciesName();
        }

        // 5 dernières captures
        List<CaptureResponse> recent = captureRepository
                .findTop5ByUserIdOrderByCaughtAtDesc(targetId)
                .stream()
                .map(CaptureResponse::from)
                .toList();

        // Badges
        List<BadgeResponse> badges = badgeRepository.findByUserId(targetId)
                .stream().map(BadgeResponse::from).toList();

        // Relation avec l'utilisateur courant
        String friendshipStatus = null;
        Long friendshipId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            userRepository.findByEmail(currentUserEmail).ifPresent(me -> {
                // handled below outside lambda — see local variables
            });
            User me = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (me != null && !me.getId().equals(targetId)) {
                Optional<Friendship> f = friendshipRepository.findBetween(me, target);
                if (f.isPresent()) {
                    Friendship fr = f.get();
                    if (fr.getStatus() == Friendship.Status.ACCEPTED) {
                        friendshipStatus = "ACCEPTED";
                    } else if (fr.getRequester().getId().equals(me.getId())) {
                        friendshipStatus = "PENDING_SENT";
                    } else {
                        friendshipStatus = "PENDING_RECEIVED";
                    }
                    friendshipId = fr.getId();
                } else {
                    friendshipStatus = "NONE";
                }
            }
        }

        return PublicProfileResponse.builder()
                .userId(targetId)
                .username(target.getUsername())
                .userTag(String.format("%04d", targetId % 10000))
                .memberSince(target.getCreatedAt() != null
                        ? target.getCreatedAt() : null)
                .totalCaptures(totalCaptures)
                .distinctSpecies(distinctSpecies)
                .heaviestCatchKg(heaviestKg)
                .heaviestCatchSpecies(heaviestSpecies)
                .recentCaptures(recent)
                .badges(badges)
                .friendshipStatus(friendshipStatus)
                .friendshipId(friendshipId)
                .build();
    }

    // ── Statistiques personnelles détaillées ──────────────────────────────

    @Transactional(readOnly = true)
    public PersonalStatsResponse getPersonalStats(String email) {
        User user = loadUserByEmail(email);
        Long userId = user.getId();

        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime startYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long totalCaptures   = captureRepository.countByUserId(userId);
        long thisYear        = captureRepository.countByUserIdAndCaughtAtAfter(userId, startYear);
        long thisMonth       = captureRepository.countByUserIdAndCaughtAtAfter(userId, startMonth);
        long distinctSpecies = captureRepository.countDistinctSpeciesByUserId(userId);

        // Capture la plus lourde
        Double heaviestKg      = null;
        String heaviestSpecies = null;
        Optional<Capture> heaviest = captureRepository.findHeaviestByUserId(userId);
        if (heaviest.isPresent()) {
            heaviestKg      = heaviest.get().getWeight();
            heaviestSpecies = heaviest.get().getSpeciesName();
        }

        // Capture la plus longue
        Double longestCm      = null;
        String longestSpecies = null;
        List<Capture> longestList = captureRepository.findLongestByUserId(userId, PageRequest.of(0, 1));
        if (!longestList.isEmpty()) {
            longestCm      = longestList.get(0).getLength();
            longestSpecies = longestList.get(0).getSpeciesName();
        }

        // Captures mensuelles — 12 derniers mois
        LocalDateTime since12months = now.minusMonths(12);
        List<Object[]> monthRows = captureRepository.findMonthlyCaptures(userId, since12months);
        List<PersonalStatsResponse.MonthStat> monthlyCaptures = new ArrayList<>();
        for (Object[] row : monthRows) {
            int month  = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            String label = java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            monthlyCaptures.add(PersonalStatsResponse.MonthStat.builder()
                    .month(month).label(label).count(count).build());
        }

        // Top espèces avec records
        List<Object[]> speciesRows = captureRepository
                .findTopSpeciesWithRecords(userId, PageRequest.of(0, 5));
        List<PersonalStatsResponse.SpeciesRecord> topSpecies = new ArrayList<>();
        for (Object[] row : speciesRows) {
            topSpecies.add(PersonalStatsResponse.SpeciesRecord.builder()
                    .speciesName((String) row[0])
                    .count(((Number) row[1]).longValue())
                    .recordWeight(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                    .recordLength(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                    .build());
        }

        // Spots favoris
        List<Object[]> spotRows = captureRepository.findFavoriteSpots(userId);
        List<PersonalStatsResponse.SpotStat> favoriteSpots = new ArrayList<>();
        for (Object[] row : spotRows) {
            double lat = ((Number) row[0]).doubleValue();
            double lng = ((Number) row[1]).doubleValue();
            long   cnt = ((Number) row[2]).longValue();
            favoriteSpots.add(PersonalStatsResponse.SpotStat.builder()
                    .lat(lat).lng(lng).count(cnt).label("Spot favori").build());
        }

        return PersonalStatsResponse.builder()
                .totalCaptures(totalCaptures)
                .thisYear(thisYear)
                .thisMonth(thisMonth)
                .distinctSpecies(distinctSpecies)
                .heaviestCatchKg(heaviestKg)
                .heaviestCatchSpecies(heaviestSpecies)
                .longestCatchCm(longestCm)
                .longestCatchSpecies(longestSpecies)
                .monthlyCaptures(monthlyCaptures)
                .topSpecies(topSpecies)
                .favoriteSpots(favoriteSpots)
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
