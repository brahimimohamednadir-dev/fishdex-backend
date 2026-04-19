package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.BadgeResponse;
import com.fishdex.backend.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges(Authentication authentication) {
        List<BadgeResponse> badges = badgeService.getMyBadges(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(badges));
    }
}
