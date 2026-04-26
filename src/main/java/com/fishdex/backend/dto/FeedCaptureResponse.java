package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.CaptureComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Réponse enrichie pour le feed social — inclut les réactions et commentaires.
 */
@Data
@Builder
public class FeedCaptureResponse {

    // ── Capture ───────────────────────────────────────────────────────────
    private Long id;
    private Long userId;
    private String username;
    private String speciesName;
    private Double weight;
    private Double length;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String note;
    private LocalDateTime caughtAt;
    private LocalDateTime createdAt;
    private String visibility;

    // ── Espèce catalogue (optionnel) ──────────────────────────────────────
    private Long speciesId;
    private String speciesLatinName;
    private String speciesImageUrl;

    // ── Social ────────────────────────────────────────────────────────────
    private int likeCount;
    private boolean hasLiked;
    private int commentCount;
    private List<CommentPreview> recentComments;

    // ── Inner DTO ─────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class CommentPreview {
        private Long id;
        private Long userId;
        private String username;
        private String content;
        private LocalDateTime createdAt;

        public static CommentPreview from(CaptureComment c) {
            return CommentPreview.builder()
                    .id(c.getId())
                    .userId(c.getUser().getId())
                    .username(c.getUser().getUsername())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────

    public static FeedCaptureResponse from(Capture capture,
                                            boolean hasLiked,
                                            int likeCount,
                                            int commentCount,
                                            List<CaptureComment> recentComments) {
        return FeedCaptureResponse.builder()
                .id(capture.getId())
                .userId(capture.getUser().getId())
                .username(capture.getUser().getUsername())
                .speciesName(capture.getSpeciesName())
                .weight(capture.getWeight())
                .length(capture.getLength())
                .photoUrl(capture.getPhotoUrl())
                .latitude(capture.getLatitude())
                .longitude(capture.getLongitude())
                .note(capture.getNote())
                .caughtAt(capture.getCaughtAt())
                .createdAt(capture.getCreatedAt())
                .visibility(capture.getVisibility().name())
                .speciesId(capture.getSpecies() != null ? capture.getSpecies().getId() : null)
                .speciesLatinName(capture.getSpecies() != null ? capture.getSpecies().getLatinName() : null)
                .speciesImageUrl(capture.getSpecies() != null ? capture.getSpecies().getImageUrl() : null)
                .likeCount(likeCount)
                .hasLiked(hasLiked)
                .commentCount(commentCount)
                .recentComments(recentComments.stream().map(CommentPreview::from).toList())
                .build();
    }
}
