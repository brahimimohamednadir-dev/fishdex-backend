package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.LeaderboardEntry;
import com.fishdex.backend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * GET /api/leaderboard?type=captures|weight|species&period=alltime|month|year
     * Public — accessible sans auth
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(
            @RequestParam(defaultValue = "captures") String type,
            @RequestParam(defaultValue = "alltime") String period) {

        List<LeaderboardEntry> entries = leaderboardService.getLeaderboard(type, period);
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }
}
