package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.LoginResponse;
import com.fishdex.backend.dto.TotpSetupResponse;
import com.fishdex.backend.dto.TotpVerifyRequest;
import com.fishdex.backend.dto.TwoFactorLoginRequest;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.service.AuthService;
import com.fishdex.backend.service.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints 2FA — tous sous /api/2fa (correspond au frontend Angular).
 *
 * Routes non-authentifiées :
 *   POST /api/2fa/verify  — étape 2 du login (tempToken + code)
 *
 * Routes authentifiées :
 *   GET  /api/2fa/status  — statut
 *   POST /api/2fa/setup   — générer secret
 *   POST /api/2fa/enable  — activer
 *   POST /api/2fa/disable — désactiver
 */
@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TotpController {

    private final TotpService totpService;
    private final AuthService authService;
    private final UserRepository userRepository;

    // ── Étape 2 login 2FA (public — pas d'auth requise) ──────────────────

    /**
     * POST /api/2fa/verify
     * Frontend envoie : { code, tempToken, trustDevice }
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verify(
            @Valid @RequestBody TwoFactorLoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = extractIp(httpRequest);
        LoginResponse response = authService.verifyTwoFactor(request, ip);
        return ResponseEntity.ok(ApiResponse.ok("Authentification 2FA réussie", response));
    }

    // ── Routes authentifiées ──────────────────────────────────────────────

    /** GET /api/2fa/status */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "enabled", Boolean.TRUE.equals(user.getTwoFactorEnabled())
        )));
    }

    /** POST /api/2fa/setup */
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<TotpSetupResponse>> setup(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUser(userDetails);
        TotpSetupResponse response = totpService.setupTotp(user);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** POST /api/2fa/enable — corps : { code } */
    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<Void>> enable(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TotpVerifyRequest request) {

        User user = getUser(userDetails);
        totpService.enableTotp(user, request.getCode());
        return ResponseEntity.ok(ApiResponse.ok("Authentification à deux facteurs activée", null));
    }

    /**
     * POST /api/2fa/disable
     * Frontend envoie : { password } (mot de passe pour confirmer la désactivation)
     */
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DisableRequest request) {

        User user = getUser(userDetails);
        totpService.disableTotp(user, request.password());
        return ResponseEntity.ok(ApiResponse.ok("Authentification à deux facteurs désactivée", null));
    }

    // ── Inner record ──────────────────────────────────────────────────────

    /** Le frontend envoie { password } pour désactiver la 2FA */
    record DisableRequest(String password) {}

    // ── Helpers ───────────────────────────────────────────────────────────

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
