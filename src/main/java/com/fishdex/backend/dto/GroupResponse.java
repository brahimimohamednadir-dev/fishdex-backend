package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Group;
import com.fishdex.backend.entity.GroupJoinRequest;
import com.fishdex.backend.entity.GroupMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private String coverPhotoUrl;
    private String visibility;
    private String category;
    private String rules;
    private long memberCount;
    private int postCount;
    private int unreadCount;
    private boolean isPro;
    private String creatorUsername;
    private LocalDateTime createdAt;
    private String myRole;
    private String myStatus;

    public static GroupResponse from(Group group, long memberCount,
                                     GroupMember myMembership,
                                     GroupJoinRequest myRequest) {
        String myRole = myMembership != null ? myMembership.getRole().name() : null;

        String myStatus = null;
        if (myMembership != null) {
            myStatus = "MEMBER";
        } else if (myRequest != null) {
            switch (myRequest.getStatus()) {
                case PENDING -> myStatus = "PENDING";
                case REJECTED -> myStatus = "REJECTED";
                case ACCEPTED -> myStatus = "MEMBER";
            }
        }

        boolean isPro = group.getCategory() == Group.GroupCategory.CLUB
                || group.getCategory() == Group.GroupCategory.ASSOCIATION;

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .coverPhotoUrl(group.getCoverPhotoUrl())
                .visibility(group.getVisibility().name())
                .category(group.getCategory().name())
                .rules(group.getRules())
                .memberCount(memberCount)
                .postCount(group.getPostCount())
                .unreadCount(0)
                .isPro(isPro)
                .creatorUsername(group.getCreator().getUsername())
                .createdAt(group.getCreatedAt())
                .myRole(myRole)
                .myStatus(myStatus)
                .build();
    }

    /** Convenience factory without membership context (public discover) */
    public static GroupResponse from(Group group, long memberCount) {
        return from(group, memberCount, null, null);
    }
}
