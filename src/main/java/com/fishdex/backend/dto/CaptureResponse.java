package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaptureResponse {

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

    public static CaptureResponse from(Capture capture) {
        return CaptureResponse.builder()
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
                .build();
    }
}
