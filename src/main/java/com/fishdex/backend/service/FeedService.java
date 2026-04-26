package com.fishdex.backend.service;

import com.fishdex.backend.dto.FeedCaptureResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.CaptureComment;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.CaptureCommentRepository;
import com.fishdex.backend.repository.CaptureReactionRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final CaptureRepository captureRepository;
    private final FriendshipRepository friendshipRepository;
    private final CaptureReactionRepository reactionRepository;
    private final CaptureCommentRepository commentRepository;

    // ── Feed principal ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<FeedCaptureResponse> getFeed(User me, Pageable pageable) {
        List<Long> friendIds = friendshipRepository.findFriendIds(me);

        Page<Capture> captures;
        if (friendIds.isEmpty()) {
            // Sans amis : afficher les captures publiques pour découvrir la communauté
            captures = captureRepository.findPublicCaptures(pageable);
        } else {
            captures = captureRepository.findFeedCaptures(friendIds, me.getId(), pageable);
        }

        List<FeedCaptureResponse> items = captures.getContent().stream()
                .map(c -> enrich(c, me))
                .toList();

        return new PageImpl<>(items, pageable, captures.getTotalElements());
    }

    // ── Nouvelles captures depuis une date (pour le bandeau "X nouvelles") ──

    @Transactional(readOnly = true)
    public long countNewSince(User me, LocalDateTime since) {
        List<Long> friendIds = friendshipRepository.findFriendIds(me);
        if (friendIds.isEmpty()) return 0L;
        return captureRepository.countNewFeedCaptures(friendIds, me.getId(), since);
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    private FeedCaptureResponse enrich(Capture capture, User me) {
        boolean hasLiked = reactionRepository.existsByCaptureIdAndUserId(capture.getId(), me.getId());
        int likeCount    = reactionRepository.countByCaptureId(capture.getId());
        int commentCount = commentRepository.countByCaptureId(capture.getId());
        List<CaptureComment> recent = commentRepository.findTopByCaptureId(
                capture.getId(), PageRequest.of(0, 2));

        return FeedCaptureResponse.from(capture, hasLiked, likeCount, commentCount, recent);
    }
}
