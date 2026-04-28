package com.fishdex.backend.dto;

import com.fishdex.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String username;
    /** Tag discriminant ex. "0042" — affiché comme username#0042 dans le frontend */
    private String userTag;
    private Boolean isPremium;
    private Integer captureCount;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userTag(user.getId() != null ? String.format("%04d", user.getId() % 10000) : "0000")
                .isPremium(user.getIsPremium())
                .captureCount(user.getCaptureCount())
                .emailVerified(user.getEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
