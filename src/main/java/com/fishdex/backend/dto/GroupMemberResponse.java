package com.fishdex.backend.dto;

import com.fishdex.backend.entity.GroupMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupMemberResponse {

    private Long userId;
    private String username;
    private String role;
    private LocalDateTime joinedAt;
    private long captureCount;

    public static GroupMemberResponse from(GroupMember member, long captureCount) {
        return GroupMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .captureCount(captureCount)
                .build();
    }
}
