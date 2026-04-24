package com.fishdex.backend.service;

import com.fishdex.backend.dto.CommentResponse;
import com.fishdex.backend.dto.PostResponse;
import com.fishdex.backend.dto.ReactionResponse;
import com.fishdex.backend.entity.*;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostReactionRepository reactionRepository;
    private final PostCommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostReportRepository reportRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CaptureRepository captureRepository;
    private final NotificationService notificationService;

    // ── Créer un post ─────────────────────────────────────────────────────

    @Transactional
    public PostResponse createPost(Long groupId, String content, Long captureId, User user) {
        Group group = loadGroup(groupId);
        requireMember(groupId, user);

        Capture capture = null;
        if (captureId != null) {
            capture = captureRepository.findById(captureId)
                    .orElseThrow(() -> new BusinessException("Capture introuvable", HttpStatus.NOT_FOUND));
        }

        Post post = Post.builder()
                .group(group)
                .user(user)
                .content(content)
                .capture(capture)
                .build();
        post = postRepository.save(post);

        // Incrémenter postCount sur le groupe
        group.setPostCount(group.getPostCount() + 1);
        groupRepository.save(group);

        return toPostResponse(post, user, group);
    }

    // ── Feed paginé ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Long groupId, Pageable pageable, User currentUser) {
        Group group = loadGroup(groupId);

        // Groupes PRIVATE/SECRET : seuls les membres voient le contenu
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        if (group.getVisibility() != Group.GroupVisibility.PUBLIC && !isMember) {
            throw new BusinessException("Accès refusé — rejoignez le groupe pour voir le feed", HttpStatus.FORBIDDEN);
        }

        return postRepository.findByGroupIdOrderByPinnedDescCreatedAtDesc(groupId, pageable)
                .map(post -> toPostResponse(post, currentUser, group));
    }

    // ── Modifier un post ──────────────────────────────────────────────────

    @Transactional
    public PostResponse updatePost(Long groupId, Long postId, String content, User user) {
        Group group = loadGroup(groupId);
        Post post = loadPost(postId, groupId);

        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Non autorisé", HttpStatus.FORBIDDEN);
        }
        // Fenêtre de modification : 15 minutes
        if (post.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
            throw new BusinessException("La fenêtre de modification (15 min) est expirée", HttpStatus.FORBIDDEN);
        }

        post.setContent(content);
        post.setEditedAt(LocalDateTime.now());
        post = postRepository.save(post);
        return toPostResponse(post, user, group);
    }

    // ── Supprimer un post ─────────────────────────────────────────────────

    @Transactional
    public void deletePost(Long groupId, Long postId, User user) {
        Group group = loadGroup(groupId);
        Post post = loadPost(postId, groupId);

        boolean isAuthor = post.getUser().getId().equals(user.getId());
        boolean isAdminOrMod = isAdminOrMod(groupId, user);
        if (!isAuthor && !isAdminOrMod) {
            throw new BusinessException("Non autorisé", HttpStatus.FORBIDDEN);
        }

        postRepository.delete(post);
        group.setPostCount(Math.max(0, group.getPostCount() - 1));
        groupRepository.save(group);
    }

    // ── Épingler / désépingler ────────────────────────────────────────────

    @Transactional
    public void pinPost(Long groupId, Long postId, User user) {
        loadGroup(groupId);
        requireAdminOrMod(groupId, user);
        Post post = loadPost(postId, groupId);
        post.setPinned(true);
        postRepository.save(post);

        // Notification à l'auteur
        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.create(
                    post.getUser(), Notification.NotificationType.POST_PINNED,
                    user.getUsername(), post.getGroup().getName(),
                    groupId, postId);
        }
    }

    @Transactional
    public void unpinPost(Long groupId, Long postId, User user) {
        loadGroup(groupId);
        requireAdminOrMod(groupId, user);
        Post post = loadPost(postId, groupId);
        post.setPinned(false);
        postRepository.save(post);
    }

    // ── Réactions ─────────────────────────────────────────────────────────

    @Transactional
    public void addReaction(Long groupId, Long postId, PostReaction.ReactionType type, User user) {
        loadGroup(groupId);
        requireMember(groupId, user);
        Post post = loadPost(postId, groupId);

        Optional<PostReaction> existing = reactionRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isPresent()) {
            PostReaction r = existing.get();
            r.setType(type);
            reactionRepository.save(r);
        } else {
            reactionRepository.save(PostReaction.builder()
                    .post(post).user(user).type(type).build());

            // Notification à l'auteur
            notificationService.create(
                    post.getUser(), Notification.NotificationType.POST_REACTION,
                    user.getUsername(), post.getGroup().getName(),
                    groupId, postId);
        }
    }

    @Transactional
    public void removeReaction(Long groupId, Long postId, User user) {
        loadGroup(groupId);
        reactionRepository.deleteByPostIdAndUserId(postId, user.getId());
    }

    // ── Commentaires ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long groupId, Long postId, User currentUser) {
        loadGroup(groupId);
        loadPost(postId, groupId);
        boolean isAdminOrMod = isAdminOrMod(groupId, currentUser);

        List<PostComment> roots = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
        return roots.stream()
                .map(c -> buildCommentResponse(c, currentUser, isAdminOrMod))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long groupId, Long postId, String content, Long parentId, User user) {
        loadGroup(groupId);
        requireMember(groupId, user);
        Post post = loadPost(postId, groupId);

        PostComment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException("Commentaire parent introuvable", HttpStatus.NOT_FOUND));
        }

        PostComment comment = PostComment.builder()
                .post(post).user(user).content(content).parent(parent).build();
        comment = commentRepository.save(comment);

        // Notifications
        if (parent == null) {
            // Commentaire → notif à l'auteur du post
            notificationService.create(
                    post.getUser(), Notification.NotificationType.POST_COMMENT,
                    user.getUsername(), post.getGroup().getName(), groupId, postId);
        } else {
            // Réponse → notif à l'auteur du commentaire parent
            notificationService.create(
                    parent.getUser(), Notification.NotificationType.COMMENT_REPLY,
                    user.getUsername(), post.getGroup().getName(), groupId, postId);
        }

        boolean liked = false;
        return CommentResponse.from(comment, user, liked, List.of(), false);
    }

    @Transactional
    public void deleteComment(Long groupId, Long postId, Long commentId, User user) {
        loadGroup(groupId);
        loadPost(postId, groupId);
        boolean isAdminOrMod = isAdminOrMod(groupId, user);

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Commentaire introuvable", HttpStatus.NOT_FOUND));

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        if (!isAuthor && !isAdminOrMod) {
            throw new BusinessException("Non autorisé", HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long groupId, Long postId, Long commentId, User user) {
        loadGroup(groupId);
        loadPost(postId, groupId);
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("Commentaire introuvable", HttpStatus.NOT_FOUND));

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            // toggle off
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, user.getId());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            commentLikeRepository.save(CommentLike.builder().comment(comment).user(user).build());
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        commentRepository.save(comment);
    }

    // ── Signaler ──────────────────────────────────────────────────────────

    @Transactional
    public void reportPost(Long groupId, Long postId, String reason, User user) {
        loadGroup(groupId);
        requireMember(groupId, user);
        Post post = loadPost(postId, groupId);

        PostReport report = PostReport.builder()
                .post(post).reporter(user).reason(reason).build();
        reportRepository.save(report);
    }

    // ── Helpers internes ──────────────────────────────────────────────────

    private PostResponse toPostResponse(Post post, User currentUser, Group group) {
        long postId = post.getId();
        long groupId = group.getId();

        // Réactions
        List<Object[]> rawReactions = reactionRepository.countByTypeForPost(postId);
        Optional<PostReaction> myReaction = reactionRepository.findByPostIdAndUserId(postId, currentUser.getId());
        String myReactionType = myReaction.map(r -> r.getType().name()).orElse(null);
        long totalReactions = 0;
        List<ReactionResponse> reactions = new ArrayList<>();
        for (PostReaction.ReactionType rt : PostReaction.ReactionType.values()) {
            long count = rawReactions.stream()
                    .filter(row -> row[0].toString().equals(rt.name()))
                    .mapToLong(row -> (Long) row[1]).sum();
            totalReactions += count;
            boolean reacted = rt.name().equals(myReactionType);
            reactions.add(new ReactionResponse(rt.name(), count, reacted));
        }

        // Commentaires (2 premiers)
        boolean isAdminOrMod = isAdminOrMod(groupId, currentUser);
        List<PostComment> allComments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
        List<CommentResponse> commentResponses = allComments.stream()
                .limit(2)
                .map(c -> buildCommentResponse(c, currentUser, isAdminOrMod))
                .collect(Collectors.toList());

        // canEdit / canDelete / canPin
        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean canEdit = isAuthor && post.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(15));
        boolean canDelete = isAuthor || isAdminOrMod;
        boolean canPin = isAdminOrMod;

        // Reported
        boolean reported = reportRepository.findByPostGroupId(groupId).stream()
                .anyMatch(r -> r.getPost() != null && r.getPost().getId().equals(post.getId())
                        && r.getReporter().getId().equals(currentUser.getId()));

        return PostResponse.builder()
                .id(post.getId())
                .groupId(groupId)
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .content(post.getContent())
                .photoUrls(List.of())
                .capture(PostResponse.PostCaptureDto.from(post.getCapture()))
                .reactions(reactions)
                .totalReactions(totalReactions)
                .commentCount(allComments.size())
                .comments(commentResponses)
                .pinned(post.isPinned())
                .createdAt(post.getCreatedAt())
                .editedAt(post.getEditedAt())
                .canEdit(canEdit)
                .canDelete(canDelete)
                .canPin(canPin)
                .reported(reported)
                .build();
    }

    private CommentResponse buildCommentResponse(PostComment comment, User currentUser, boolean isAdminOrMod) {
        boolean liked = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUser.getId());
        List<PostComment> children = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
        List<CommentResponse> replies = children.stream()
                .map(c -> buildCommentResponse(c, currentUser, isAdminOrMod))
                .collect(Collectors.toList());
        return CommentResponse.from(comment, currentUser, liked, replies, isAdminOrMod);
    }

    private boolean isAdminOrMod(Long groupId, User user) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .map(m -> m.getRole() == GroupMember.MemberRole.ADMIN
                        || m.getRole() == GroupMember.MemberRole.MODERATOR
                        || m.getRole() == GroupMember.MemberRole.OWNER)
                .orElse(false);
    }

    private void requireMember(Long groupId, User user) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("Vous devez être membre du groupe", HttpStatus.FORBIDDEN);
        }
    }

    private void requireAdminOrMod(Long groupId, User user) {
        if (!isAdminOrMod(groupId, user)) {
            throw new BusinessException("Action réservée aux administrateurs", HttpStatus.FORBIDDEN);
        }
    }

    private Group loadGroup(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Groupe introuvable", HttpStatus.NOT_FOUND));
    }

    private Post loadPost(Long postId, Long groupId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post introuvable", HttpStatus.NOT_FOUND));
        if (!post.getGroup().getId().equals(groupId)) {
            throw new BusinessException("Post introuvable dans ce groupe", HttpStatus.NOT_FOUND);
        }
        return post;
    }
}
