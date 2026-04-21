package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Correspond à l'interface FeedItem du frontend Angular :
 * { captureId, userId, username, speciesName, weight, length, photoUrl, caughtAt, createdAt }
 */
@Data
@Builder
public class FeedItemResponse {

    private Long captureId;
    private Long userId;
    private String username;
    private String speciesName;
    private Double weight;
    private Double length;
    private String photoUrl;
    private LocalDateTime caughtAt;
    private LocalDateTime createdAt;

    public static FeedItemResponse from(Capture capture) {
        return FeedItemResponse.builder()
                .captureId(capture.getId())
                .userId(capture.getUser().getId())
                .username(capture.getUser().getUsername())
                .speciesName(capture.getSpeciesName())
                .weight(capture.getWeight())
                .length(capture.getLength())
                .photoUrl(capture.getPhotoUrl())
                .caughtAt(capture.getCaughtAt())
                .createdAt(capture.getCreatedAt())
                .build();
    }
}
