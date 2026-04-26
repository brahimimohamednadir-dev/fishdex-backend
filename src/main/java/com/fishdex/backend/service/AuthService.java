package com.fishdex.backend.service;

import com.fishdex.backend.dto.*;
import com.fishdex.backend.entity.PreAuthToken;
import com.fishdex.backend.entity.RefreshToken;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.UserRepository;
import com.fishdex.backend.security.JwtService;
import com.fishdex.backend.service.GoogleAuthService.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final LoginAttemptService loginAttemptService;
    private final TotpService totpService;
    private final PreAuthTokenService preAuthTokenService;
    private final GoogleAuthService googleAuthService;

    // ── UserDetailsService ────────────────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }

    // ── Register ──────────────────────────────────────────────────────────

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String username = request.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .userTag(generateUniqueTag())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .build();

        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré : {}", saved.getEmail());

        // Envoi de l'email de vérification (asynchrone, fault-tolerant)
        emailVerificationService.sendVerificationEmail(saved);

        return UserResponse.from(saved);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED));

        // Vérification du verrouillage
        if (user.isLocked()) {
            throw new BusinessException(
                    "Compte temporairement bloqué suite à trop de tentatives. Réessayez dans quelques minutes.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        // Vérification du mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(user);
            throw new BusinessException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
        }

        // Connexion réussie — reset du compteur
        loginAttemptService.recordSuccess(user);

        // Vérification 2FA
        if (totpService.isTotpEnabled(user)) {
            PreAuthToken preAuthToken = preAuthTokenService.create(user);
            log.info("2FA requise pour {}", user.getEmail());
            return LoginResponse.builder()
                    .requiresTwoFactor(true)
                    .tempToken(preAuthToken.getToken())
                    .build();
        }

        return buildLoginResponse(user, request.getRememberMe(), request.getDeviceInfo(), ipAddress);
    }

    // ── 2FA verification ──────────────────────────────────────────────────

    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorLoginRequest request, String ipAddress) {
        PreAuthToken preAuth = preAuthTokenService.validateAndConsume(request.getTempToken());
        User user = preAuth.getUser();

        if (!totpService.verifyCode(user, request.getCode())) {
            throw new BusinessException("Code TOTP invalide", HttpStatus.UNAUTHORIZED);
        }

        log.info("2FA validée pour {}", user.getEmail());
        return buildLoginResponse(user, Boolean.TRUE.equals(request.getRememberMe()), request.getDeviceInfo(), ipAddress);
    }

    // ── Google OAuth ──────────────────────────────────────────────────────

    @Transactional
    public LoginResponse loginWithGoogle(GoogleAuthRequest request, String ipAddress) {
        GoogleUserInfo info = googleAuthService.verifyIdToken(request.getIdToken());

        // Chercher par googleId ou par email
        User user = userRepository.findByGoogleId(info.googleId())
                .or(() -> userRepository.findByEmail(info.email().toLowerCase()))
                .orElseGet(() -> {
                    // Création automatique du compte Google
                    String username = generateUsernameFromEmail(info.email());
                    return User.builder()
                            .email(info.email().toLowerCase())
                            .username(username)
                            .userTag(generateUniqueTag())
                            .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                            .googleId(info.googleId())
                            .emailVerified(true)
                            .emailVerifiedAt(java.time.LocalDateTime.now())
                            .build();
                });

        // Lier le googleId si ce n'est pas déjà fait
        if (user.getGoogleId() == null) {
            user.setGoogleId(info.googleId());
            if (!user.getEmailVerified()) {
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(java.time.LocalDateTime.now());
            }
        }

        user = userRepository.save(user);
        log.info("Connexion Google réussie pour {}", user.getEmail());

        return buildLoginResponse(user, false, request.getDeviceInfo(), ipAddress);
    }

    // ── Refresh ───────────────────────────────────────────────────────────

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.getRefreshToken());
        User user = refreshToken.getUser();

        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateToken(userDetails);

        return RefreshTokenResponse.builder()
                .token(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
        refreshTokenService.revokeByUser(user);
        log.info("Déconnexion de l'utilisateur : {}", email);
    }

    /** Déconnexion d'une session spécifique */
    @Transactional
    public void logoutSession(String email, Long sessionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
        boolean revoked = refreshTokenService.revokeById(sessionId, user);
        if (!revoked) {
            throw new BusinessException("Session non trouvée", HttpStatus.NOT_FOUND);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private LoginResponse buildLoginResponse(User user, Boolean rememberMe, String deviceInfo, String ipAddress) {
        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        boolean remember = Boolean.TRUE.equals(rememberMe);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, remember, deviceInfo, ipAddress);

        return LoginResponse.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(refreshToken.getToken())
                .user(UserResponse.from(user))
                .requiresTwoFactor(false)
                .build();
    }

    /**
     * Génère un tag unique à 5 chiffres (00000–99999).
     * Réessaie jusqu'à trouver un tag libre (max 100 tentatives).
     */
    private String generateUniqueTag() {
        SecureRandom rng = new SecureRandom();
        for (int i = 0; i < 100; i++) {
            String tag = String.format("%05d", rng.nextInt(100_000));
            if (!userRepository.existsByUserTag(tag)) return tag;
        }
        throw new BusinessException("Impossible de générer un tag unique", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String generateUsernameFromEmail(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (base.length() < 3) base = "user" + base;
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
