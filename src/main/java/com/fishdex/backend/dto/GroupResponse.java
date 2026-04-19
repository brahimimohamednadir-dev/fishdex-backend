package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Group;
import com.fishdex.backend.entity.Group.GroupType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private GroupType type;
    private Boolean isPro;
    private Long memberCount;
    private String creatorUsername;
    private LocalDateTime createdAt;

    public static GroupResponse from(Group g, long memberCount) {
        return GroupResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .type(g.getType())
                .isPro(g.getIsPro())
                .memberCount(memberCount)
                .creatorUsername(g.getCreator().getUsername())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
