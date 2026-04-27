package com.fishdex.backend.service;

import com.fishdex.backend.dto.LeaderboardEntry;
import com.fishdex.backend.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(String type, String period) {
        LocalDateTime since = switch (period) {
            case "month" -> LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "year"  -> LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default      -> LocalDateTime.of(2000, 1, 1, 0, 0);
        };

        List<Object[]> rows = switch (type) {
            case "weight"  -> leaderboardRepository.rankByWeight(since);
            case "species" -> leaderboardRepository.rankBySpecies(since);
            default        -> leaderboardRepository.rankByCaptures(since);
        };

        List<LeaderboardEntry> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Long userId       = ((Number) row[0]).longValue();
            String username   = (String) row[1];
            String userTag    = row[2] != null ? (String) row[2] : "00000";
            long captures     = ((Number) row[3]).longValue();
            double weight     = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            long species      = ((Number) row[5]).longValue();
            double score      = switch (type) {
                case "weight"  -> weight;
                case "species" -> (double) species;
                default        -> (double) captures;
            };

            result.add(LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(username)
                    .userTag(userTag)
                    .totalCaptures(captures)
                    .totalWeight(weight)
                    .distinctSpecies(species)
                    .score(score)
                    .build());
        }
        return result;
    }
}
