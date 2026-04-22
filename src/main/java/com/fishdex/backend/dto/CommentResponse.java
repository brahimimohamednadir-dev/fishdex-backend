package com.fishdex.backend.dto;

import com.fishdex.backend.entity.PostComment;
import com.fishdex.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private int likeCount;
    private boolean liked;
    private List<CommentResponse> replies;
    private boolean canEdit;
    private boolean canDelete;

    public static CommentResponse from(PostComment comment, User currentUser,
                                       boolean liked, List<CommentResponse> replies,
                                       boolean isAdminOrMod) {
        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean canEdit = isAuthor;
        boolean canDelete = isAuthor || isAdminOrMod;

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .editedAt(comment.getEditedAt())
                .likeCount(comment.getLikeCount())
                .liked(liked)
                .replies(replies)
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }
}
