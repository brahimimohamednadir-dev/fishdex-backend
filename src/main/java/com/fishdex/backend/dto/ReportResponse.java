package com.fishdex.backend.dto;

import com.fishdex.backend.entity.PostReport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {

    private Long id;
    private Long reporterId;
    private String reporterUsername;
    private String targetType;
    private Long targetId;
    private String contentPreview;
    private String reason;
    private LocalDateTime createdAt;

    public static ReportResponse from(PostReport r) {
        String targetType;
        Long targetId;
        String preview;
        if (r.getPost() != null) {
            targetType = "POST";
            targetId = r.getPost().getId();
            String content = r.getPost().getContent();
            preview = content.length() > 100 ? content.substring(0, 100) + "…" : content;
        } else if (r.getComment() != null) {
            targetType = "COMMENT";
            targetId = r.getComment().getId();
            String content = r.getComment().getContent();
            preview = content.length() > 100 ? content.substring(0, 100) + "…" : content;
        } else {
            targetType = "UNKNOWN";
            targetId = null;
            preview = "";
        }

        return ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .reporterUsername(r.getReporter().getUsername())
                .targetType(targetType)
                .targetId(targetId)
                .contentPreview(preview)
                .reason(r.getReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
