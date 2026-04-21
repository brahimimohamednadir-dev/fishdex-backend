package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Group;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Correspond à l'interface Group du frontend Angular :
 * { id, name, description, type, isPro, memberCount, creatorUsername, createdAt }
 */
@Data
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private String type;

    /** isPro = true si type est CLUB ou ASSOCIATION (abonnement pro 10€/mois) */
    private Boolean isPro;

    private Long memberCount;
    private String creatorUsername;
    private LocalDateTime createdAt;

    public static GroupResponse from(Group group, long memberCount) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .type(group.getType().name())
                .isPro(group.getType() != Group.GroupType.PRIVATE)
                .memberCount(memberCount)
                .creatorUsername(group.getCreator().getUsername())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
