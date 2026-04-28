package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.BadgeResponse;
import com.fishdex.backend.dto.CaptureStatsResponse;
import com.fishdex.backend.dto.PersonalStatsResponse;
import com.fishdex.backend.dto.PublicProfileResponse;
import com.fishdex.backend.dto.SessionResponse;
import com.fishdex.backend.dto.UserStatsResponse;
import com.fishdex.backend.dto.UpdateUsernameRequest;
import com.fishdex.backend.dto.UserResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.service.AuthService;
import com.fishdex.backend.service.RefreshTokenService;
import com.fishdex.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;
    private final UserRepository userRepository;

    /** GET /api/users/me — Profil de l'utilisateur connecté */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(authentication.getName())));
    }

    /** PUT /api/users/me — Modifier le nom d'utilisateur */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @Valid @RequestBody UpdateUsernameRequest request,
            Authentication authentication) {
        UserResponse response = userService.updateUsername(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profil mis à jour", response));
    }

    /** GET /api/users/me/badges — Badges de l'utilisateur connecté */
    @GetMapping("/me/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges(Authentication authentication) {
        List<BadgeResponse> badges = userService.getMyBadges(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(badges));
    }

    /**
     * GET /api/users/me/stats — Statistiques (format frontend UserStats)
     * C'est la route appelée par le frontend Angular.
     */
    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getMyStats(Authentication authentication) {
        UserStatsResponse stats = userService.getMyStats(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /** GET /api/users/me/captures/stats — Statistiques détaillées (route legacy) */
    @GetMapping("/me/captures/stats")
    public ResponseEntity<ApiResponse<CaptureStatsResponse>> getMyCaptureStats(Authentication authentication) {
        CaptureStatsResponse stats = userService.getMyCaptureStats(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /** GET /api/users/me/personal-stats — Statistiques personnelles complètes */
    @GetMapping("/me/personal-stats")
    public ResponseEntity<ApiResponse<PersonalStatsResponse>> getPersonalStats(Authentication authentication) {
        PersonalStatsResponse stats = userService.getPersonalStats(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /**
     * GET /api/users/{username} — Profil public d'un utilisateur.
     * Accessible sans authentification (la relation amis est null si non connecté).
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(
            @PathVariable String username,
            Authentication authentication) {
        String currentEmail = authentication != null ? authentication.getName() : null;
        PublicProfileResponse profile = userService.getPublicProfile(username, currentEmail);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    /** GET /api/users/me/sessions — Sessions actives (multi-device) */
    @GetMapping("/me/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<SessionResponse> sessions = refreshTokenService.getActiveSessions(user)
                .stream()
                .map(SessionResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    /** DELETE /api/users/me/sessions/{id} — Révoquer une session spécifique */
    @DeleteMapping("/me/sessions/{id}")
    public ResponseEntity<Void> revokeSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        authService.logoutSession(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/users/me/sessions — Révoquer toutes les sessions (logout global) */
    @DeleteMapping("/me/sessions")
    public ResponseEntity<Void> revokeAllSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        authService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/users/me/sessions/others — Révoquer toutes les autres sessions
     * (garder la session courante active — appelé par SessionService.revokeAllOthers())
     */
    @DeleteMapping("/me/sessions/others")
    public ResponseEntity<ApiResponse<Void>> revokeOtherSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Pour l'instant : révoque tout (le frontend invalide son token local)
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Toutes les autres sessions ont été révoquées", null));
    }
}
