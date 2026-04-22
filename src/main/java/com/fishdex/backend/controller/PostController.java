package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.CommentResponse;
import com.fishdex.backend.dto.PostResponse;
import com.fishdex.backend.entity.PostReaction;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.service.PostService;
import com.fishdex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    // ── CRUD posts ────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @PathVariable Long groupId,
            @RequestBody CreatePostBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        PostResponse response = postService.createPost(groupId, body.content(), body.captureId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Post créé", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        Page<PostResponse> posts = postService.getPosts(groupId, PageRequest.of(page, size), user);
        return ResponseEntity.ok(ApiResponse.ok(posts));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody UpdatePostBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        PostResponse response = postService.updatePost(groupId, postId, body.content(), user);
        return ResponseEntity.ok(ApiResponse.ok("Post modifié", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.deletePost(groupId, postId, user);
        return ResponseEntity.ok(ApiResponse.ok("Post supprimé", null));
    }

    // ── Pin ───────────────────────────────────────────────────────────────

    @PostMapping("/{postId}/pin")
    public ResponseEntity<ApiResponse<Void>> pinPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.pinPost(groupId, postId, user);
        return ResponseEntity.ok(ApiResponse.ok("Post épinglé", null));
    }

    @DeleteMapping("/{postId}/pin")
    public ResponseEntity<ApiResponse<Void>> unpinPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.unpinPost(groupId, postId, user);
        return ResponseEntity.ok(ApiResponse.ok("Post désépinglé", null));
    }

    // ── Réactions ─────────────────────────────────────────────────────────

    @PostMapping("/{postId}/react")
    public ResponseEntity<ApiResponse<Void>> addReaction(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody ReactionBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        PostReaction.ReactionType type = PostReaction.ReactionType.valueOf(body.type().toUpperCase());
        postService.addReaction(groupId, postId, type, user);
        return ResponseEntity.ok(ApiResponse.ok("Réaction ajoutée", null));
    }

    @DeleteMapping("/{postId}/react")
    public ResponseEntity<ApiResponse<Void>> removeReaction(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.removeReaction(groupId, postId, user);
        return ResponseEntity.ok(ApiResponse.ok("Réaction retirée", null));
    }

    // ── Commentaires ──────────────────────────────────────────────────────

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(postService.getComments(groupId, postId, user)));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody CommentBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        CommentResponse comment = postService.addComment(groupId, postId, body.content(), body.parentId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Commentaire ajouté", comment));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.deleteComment(groupId, postId, commentId, user);
        return ResponseEntity.ok(ApiResponse.ok("Commentaire supprimé", null));
    }

    @PostMapping("/{postId}/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        postService.likeComment(groupId, postId, commentId, user);
        return ResponseEntity.ok(ApiResponse.ok("Like mis à jour", null));
    }

    // ── Signaler ──────────────────────────────────────────────────────────

    @PostMapping("/{postId}/report")
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody(required = false) ReportBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        String reason = body != null ? body.reason() : null;
        postService.reportPost(groupId, postId, reason, user);
        return ResponseEntity.ok(ApiResponse.ok("Signalement enregistré", null));
    }

    // ── Inner records ─────────────────────────────────────────────────────

    record CreatePostBody(String content, Long captureId) {}
    record UpdatePostBody(String content) {}
    record ReactionBody(String type) {}
    record CommentBody(String content, Long parentId) {}
    record ReportBody(String reason) {}
}
