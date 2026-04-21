package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.ForgotPasswordRequest;
import com.fishdex.backend.dto.GoogleAuthRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.LoginResponse;
import com.fishdex.backend.dto.RefreshTokenRequest;
import com.fishdex.backend.dto.RefreshTokenResponse;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.dto.ResetPasswordRequest;
import com.fishdex.backend.dto.UserResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.service.AuthService;
import com.fishdex.backend.service.EmailVerificationService;
import com.fishdex.backend.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    // ── Register ──────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Compte créé avec succès", user));
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.login(request, extractIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok("Connexion réussie", response));
    }

    // ── Google OAuth ──────────────────────────────────────────────────────
    // Note: 2FA verify est géré par TotpController (POST /api/2fa/verify)

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.loginWithGoogle(request, extractIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok("Connexion Google réussie", response));
    }

    // ── Refresh token ─────────────────────────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok("Token renouvelé", response));
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ── Email verification ────────────────────────────────────────────────

    /**
     * POST /api/auth/verify-email
     * Frontend envoie : { token } dans le body (pas en query param)
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody TokenRequest body) {
        emailVerificationService.verifyEmail(body.token());
        return ResponseEntity.ok(ApiResponse.ok("Email vérifié avec succès", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        emailVerificationService.sendVerificationEmail(user);
        return ResponseEntity.ok(ApiResponse.ok("Email de vérification renvoyé", null));
    }

    // ── Password reset ────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.initiateReset(request.getEmail());
        // Anti-énumération : toujours la même réponse
        return ResponseEntity.ok(ApiResponse.ok(
                "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé avec succès", null));
    }

    // ── Inner records ─────────────────────────────────────────────────────

    /** Corps de la requête verify-email : { token } */
    record TokenRequest(String token) {}

    // ── Helpers ───────────────────────────────────────────────────────────

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
