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

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * GET /api/users/{username}
     * viewerEmail = null si anonyme, sinon email du visiteur connecté.
     */
    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username, String viewerEmail) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));
        Long targetId = target.getId();

        long totalCaptures   = captureRepository.countByUserId(targetId);
        long distinctSpecies = captureRepository.countDistinctSpeciesByUserId(targetId);

        // Plus gros poisson
        String heaviestSpecies = null;
        Double heaviestKg      = null;
        List<Capture> topW = captureRepository.findTopByWeightForUser(targetId, PageRequest.of(0, 1));
        if (!topW.isEmpty()) {
            heaviestKg      = topW.get(0).getWeight();
            heaviestSpecies = topW.get(0).getSpeciesName();
        }

        // 9 dernières captures publiques
        List<CaptureResponse> recent = captureRepository
                .findByUserIdAndVisibilityOrderByCreatedAtDesc(
                        targetId, Capture.Visibility.PUBLIC, PageRequest.of(0, 9))
                .getContent()
                .stream()
                .map(CaptureResponse::from)
                .toList();

        // Badges
        List<BadgeResponse> badges = badgeRepository.findByUserId(targetId)
                .stream()
                .map(BadgeResponse::from)
                .toList();

        // Statut d'amitié avec le visiteur
        String friendshipStatus = null;
        Long   friendshipId     = null;
        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null && !viewer.getId().equals(targetId)) {
                Optional<Friendship> fs = friendshipRepository.findBetween(viewer, target);
                if (fs.isPresent()) {
                    Friendship f = fs.get();
                    friendshipId = f.getId();
                    if (f.getStatus() == Friendship.Status.ACCEPTED) {
                        friendshipStatus = "ACCEPTED";
                    } else if (f.getRequester().getId().equals(viewer.getId())) {
                        friendshipStatus = "PENDING_SENT";
                    } else {
                        friendshipStatus = "PENDING_RECEIVED";
                    }
                } else {
                    friendshipStatus = "NONE";
                }
            }
        }

        return PublicProfileResponse.builder()
                .userId(targetId)
                .username(target.getUsername())
                .userTag(target.getUserTag())
                .memberSince(target.getCreatedAt())
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

    // ── Stats personnelles enrichies ──────────────────────────────────────

    @Transactional(readOnly = true)
    public PersonalStatsResponse getPersonalStats(String email) {
        User user = loadUserByEmail(email);
        Long userId = user.getId();
        int currentYear = LocalDateTime.now().getYear();

        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfYear  = LocalDateTime.now()
                .withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Totaux
        long totalCaptures   = captureRepository.countByUserId(userId);
        long thisYear        = captureRepository.countByUserIdAndCaughtAtAfter(userId, startOfYear);
        long thisMonth       = captureRepository.countByUserIdAndCaughtAtAfter(userId, startOfMonth);
        long distinctSpecies = captureRepository.countDistinctSpeciesByUserId(userId);

        // Records globaux
        List<Capture> topWeight = captureRepository.findTopByWeightForUser(userId, PageRequest.of(0, 1));
        Double heaviestKg      = topWeight.isEmpty() ? null : topWeight.get(0).getWeight();
        String heaviestSpecies = topWeight.isEmpty() ? null : topWeight.get(0).getSpeciesName();
        Double longestCm       = captureRepository.findMaxLengthByUserId(userId);
        // Espèce pour la longueur max — requête simple
        String longestSpecies  = null;
        if (longestCm != null) {
            var topLen = captureRepository.findAll().stream()
                    .filter(c -> c.getUser().getId().equals(userId) && longestCm.equals(c.getLength()))
                    .findFirst();
            longestSpecies = topLen.map(Capture::getSpeciesName).orElse(null);
        }

        // Courbe mensuelle : 12 mois de l'année en cours
        Map<Integer, Long> monthlyCounts = new HashMap<>();
        captureRepository.findMonthlyCaptureCountsForYear(userId, currentYear)
                .forEach(row -> monthlyCounts.put(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue()
                ));

        String[] monthLabels = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        List<PersonalStatsResponse.MonthStat> monthlyCaptures = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthlyCaptures.add(PersonalStatsResponse.MonthStat.builder()
                    .month(m)
                    .label(monthLabels[m - 1])
                    .count(monthlyCounts.getOrDefault(m, 0L))
                    .build());
        }

        // Palmarès espèces top 10
        List<PersonalStatsResponse.SpeciesRecord> topSpecies =
                captureRepository.findSpeciesRecordsForUser(userId, PageRequest.of(0, 10))
                        .stream()
                        .map(row -> PersonalStatsResponse.SpeciesRecord.builder()
                                .speciesName((String) row[0])
                                .count(((Number) row[1]).longValue())
                                .recordWeight(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                                .recordLength(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                                .build())
                        .toList();

        // Spots favoris top 5
        List<PersonalStatsResponse.SpotStat> favoriteSpots =
                captureRepository.findFavoriteSpotsForUser(userId, PageRequest.of(0, 5))
                        .stream()
                        .map(row -> {
                            double lat = ((Number) row[0]).doubleValue();
                            double lng = ((Number) row[1]).doubleValue();
                            long   cnt = ((Number) row[2]).longValue();
                            return PersonalStatsResponse.SpotStat.builder()
                                    .lat(lat)
                                    .lng(lng)
                                    .count(cnt)
                                    .label(String.format("%.2f, %.2f", lat, lng))
                                    .build();
                        })
                        .toList();

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
