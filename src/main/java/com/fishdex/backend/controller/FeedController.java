package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.FeedCaptureResponse;
import com.fishdex.backend.entity.CaptureComment;
import com.fishdex.backend.entity.CaptureReaction;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureCommentRepository;
import com.fishdex.backend.repository.CaptureReactionRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.service.FeedService;
import com.fishdex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;
    private final CaptureRepository captureRepository;
    private final CaptureReactionRepository reactionRepository;
    private final CaptureCommentRepository commentRepository;

    /** GET /api/feed?page=0&size=20 — feed paginé */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedCaptureResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        Page<FeedCaptureResponse> feed = feedService.getFeed(me, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(feed));
    }

    /** GET /api/feed/new?since=<ISO> — nombre de nouvelles captures */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countNew(
            @RequestParam String since,
            Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        LocalDateTime sinceDate = LocalDateTime.parse(since);
        long count = feedService.countNewSince(me, sinceDate);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    // ── Like / Unlike ─────────────────────────────────────────────────────

    /** POST /api/feed/captures/{id}/like — toggle like */
    @PostMapping("/captures/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            @PathVariable Long id,
            Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        var capture = captureRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Capture introuvable", HttpStatus.NOT_FOUND));

        boolean wasLiked = reactionRepository.existsByCaptureIdAndUserId(id, me.getId());
        if (wasLiked) {
            reactionRepository.findByCaptureIdAndUserId(id, me.getId())
                    .ifPresent(reactionRepository::delete);
        } else {
            reactionRepository.save(CaptureReaction.builder()
                    .capture(capture).user(me).build());
        }
        int newCount = reactionRepository.countByCaptureId(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("liked", !wasLiked, "likeCount", newCount)));
    }

    // ── Commentaires ──────────────────────────────────────────────────────

    /** GET /api/feed/captures/{id}/comments */
    @GetMapping("/captures/{id}/comments")
    public ResponseEntity<ApiResponse<List<FeedCaptureResponse.CommentPreview>>> getComments(
            @PathVariable Long id) {
        List<CaptureComment> comments = commentRepository.findByCaptureIdOrderByCreatedAtAsc(id);
        List<FeedCaptureResponse.CommentPreview> response = comments.stream()
                .map(FeedCaptureResponse.CommentPreview::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** POST /api/feed/captures/{id}/comments */
    @PostMapping("/captures/{id}/comments")
    public ResponseEntity<ApiResponse<FeedCaptureResponse.CommentPreview>> addComment(
            @PathVariable Long id,
            @RequestBody CommentBody body,
            Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        var capture = captureRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Capture introuvable", HttpStatus.NOT_FOUND));

        if (body.content() == null || body.content().isBlank()) {
            throw new BusinessException("Le commentaire ne peut pas être vide", HttpStatus.BAD_REQUEST);
        }

        CaptureComment comment = commentRepository.save(
                CaptureComment.builder()
                        .capture(capture).user(me)
                        .content(body.content().trim())
                        .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Commentaire ajouté", FeedCaptureResponse.CommentPreview.from(comment)));
    }

    /** DELETE /api/feed/captures/{captureId}/comments/{commentId} */
    @DeleteMapping("/captures/{captureId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long captureId,
            @PathVariable Long commentId,
            Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        CaptureComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Commentaire introuvable", HttpStatus.NOT_FOUND));
        if (!comment.getUser().getId().equals(me.getId())) {
            throw new BusinessException("Accès refusé", HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok(ApiResponse.ok("Commentaire supprimé", null));
    }

    record CommentBody(String content) {}
}
