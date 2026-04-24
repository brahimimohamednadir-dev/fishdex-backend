package com.fishdex.backend.dto;

import com.fishdex.backend.entity.GroupJoinRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JoinRequestResponse {

    private Long id;
    private Long userId;
    private String username;
    private String message;
    private LocalDateTime requestedAt;

    public static JoinRequestResponse from(GroupJoinRequest request) {
        return JoinRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .message(request.getMessage())
                .requestedAt(request.getRequestedAt())
                .build();
    }
}
