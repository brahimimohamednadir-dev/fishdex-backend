package com.fishdex.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RGPD Article 20 — Portabilité des données.
 * Contient toutes les données personnelles de l'utilisateur dans un format lisible.
 */
@Value
@Builder
public class GdprExportResponse {

    // ── Identité ─────────────────────────────────────────────────────────────
    String username;
    String email;
    String userTag;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime accountCreatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime emailVerifiedAt;

    // ── Captures ──────────────────────────────────────────────────────────────
    List<CaptureExport> captures;

    // ── Métadonnées export ────────────────────────────────────────────────────
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime exportedAt;

    String notice;

    @Value
    @Builder
    public static class CaptureExport {
        Long id;
        String speciesName;
        Double weight;
        Double length;
        String location;
        Double latitude;
        Double longitude;
        String notes;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime caughtAt;
    }
}
