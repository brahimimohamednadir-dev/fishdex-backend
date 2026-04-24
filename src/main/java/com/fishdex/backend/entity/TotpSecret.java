package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Secret TOTP (RFC 6238) associé à un utilisateur.
 * Le secret est stocké encodé en Base32.
 * Les codes de secours sont stockés séparément (BCrypt hash).
 */
@Entity
@Table(name = "totp_secrets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Secret Base32 — jamais exposé en clair après la configuration */
    @Column(nullable = false, length = 64)
    private String secret;

    /** JSON array des codes de secours hashés en BCrypt */
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
