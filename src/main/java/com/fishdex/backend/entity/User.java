package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Tag unique à 5 chiffres — permet de trouver un ami précisément.
     * Format affiché : username#12345
     */
    @Column(name = "user_tag", nullable = false, length = 5, unique = true)
    private String userTag;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "capture_count", nullable = false)
    @Builder.Default
    private Integer captureCount = 0;

    // ── Email verification ────────────────────────────────────────────────

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // ── Account lockout (brute-force protection) ──────────────────────────

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // ── Google OAuth ──────────────────────────────────────────────────────

    @Column(name = "google_id", length = 100, unique = true)
    private String googleId;

    // ── 2FA ───────────────────────────────────────────────────────────────

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    // ── Timestamps ────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Helper methods ────────────────────────────────────────────────────

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
}
