package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Badge;
import com.fishdex.backend.entity.Badge.BadgeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BadgeResponse {

    private Long id;
    private String type;
    private String label;
    private LocalDateTime earnedAt;

    public static BadgeResponse from(Badge b) {
        return BadgeResponse.builder()
                .id(b.getId())
                .type(b.getType().name())
                .label(labelFor(b.getType()))
                .earnedAt(b.getEarnedAt())
                .build();
    }

    private static String labelFor(BadgeType type) {
        return switch (type) {
            case FIRST_CATCH -> "Première capture";
            case TEN_CATCHES -> "10 captures";
            case FIFTY_CATCHES -> "50 captures";
            case FIRST_GROUP -> "Premier groupe rejoint";
            case SPECIES_COLLECTOR -> "Collectionneur (5 espèces)";
            case PHOTOGRAPHER -> "Photographe (3 photos)";
        };
    }
}
