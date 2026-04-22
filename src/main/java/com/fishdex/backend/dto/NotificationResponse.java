package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private boolean read;
    private String actorUsername;
    private String groupName;
    private Long groupId;
    private Long postId;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .read(n.isRead())
                .actorUsername(n.getActorUsername())
                .groupName(n.getGroupName())
                .groupId(n.getGroupId())
                .postId(n.getPostId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
