package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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

    public static FeedItemResponse from(Capture c) {
        return FeedItemResponse.builder()
                .captureId(c.getId())
                .userId(c.getUser().getId())
                .username(c.getUser().getUsername())
                .speciesName(c.getSpeciesName())
                .weight(c.getWeight())
                .length(c.getLength())
                .photoUrl(c.getPhotoUrl())
                .caughtAt(c.getCaughtAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
