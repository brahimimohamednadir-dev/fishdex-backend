package com.fishdex.backend.dto;

import com.fishdex.backend.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {

    private Long id;
    private Long groupId;
    private Long userId;
    private String username;
    private String content;
    private List<String> photoUrls;
    private PostCaptureDto capture;
    private List<ReactionResponse> reactions;
    private long totalReactions;
    private long commentCount;
    private List<CommentResponse> comments;
    private boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canPin;
    private boolean reported;

    @Data
    @Builder
    public static class PostCaptureDto {
        private Long id;
        private String speciesName;
        private Double weight;
        private Double length;
        private String photoUrl;
        private LocalDateTime caughtAt;

        public static PostCaptureDto from(Capture c) {
            if (c == null) return null;
            return PostCaptureDto.builder()
                    .id(c.getId())
                    .speciesName(c.getSpeciesName())
                    .weight(c.getWeight())
                    .length(c.getLength())
                    .photoUrl(c.getPhotoUrl())
                    .caughtAt(c.getCaughtAt())
                    .build();
        }
    }
}
