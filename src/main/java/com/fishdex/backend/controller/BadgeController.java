package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.BadgeResponse;
import com.fishdex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /api/badges/me — badges de l'utilisateur connecté.
 * Route appelée par le frontend Angular (BadgeService.getMyBadges()).
 */
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges(Authentication authentication) {
        List<BadgeResponse> badges = userService.getMyBadges(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(badges));
    }
}
