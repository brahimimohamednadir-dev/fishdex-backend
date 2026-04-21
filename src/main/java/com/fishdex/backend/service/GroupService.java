package com.fishdex.backend.service;

import com.fishdex.backend.dto.FeedItemResponse;
import com.fishdex.backend.dto.GroupResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.Group;
import com.fishdex.backend.entity.GroupMember;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CaptureRepository captureRepository;
    private final BadgeService badgeService;

    // ── Création ──────────────────────────────────────────────────────────

    @Transactional
    public GroupResponse createGroup(String name, String description, String type, User creator) {
        Group.GroupType groupType;
        try {
            groupType = Group.GroupType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Type de groupe invalide : " + type, HttpStatus.BAD_REQUEST);
        }

        Group group = Group.builder()
                .name(name.trim())
                .description(description)
                .type(groupType)
                .creator(creator)
                .build();

        Group saved = groupRepository.save(group);

        // Créateur devient ADMIN automatiquement
        GroupMember member = GroupMember.builder()
                .group(saved)
                .user(creator)
                .role(GroupMember.MemberRole.ADMIN)
                .build();
        groupMemberRepository.save(member);

        // Badge "Premier groupe"
        badgeService.awardFirstGroup(creator);

        log.info("Groupe '{}' créé par {}", name, creator.getEmail());
        return GroupResponse.from(saved, 1L);
    }

    // ── Lecture ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Groupe introuvable", HttpStatus.NOT_FOUND));
        long memberCount = groupMemberRepository.countByGroupId(id);
        return GroupResponse.from(group, memberCount);
    }

    // ── Rejoindre ─────────────────────────────────────────────────────────

    @Transactional
    public void joinGroup(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Groupe introuvable", HttpStatus.NOT_FOUND));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("Vous êtes déjà membre de ce groupe", HttpStatus.CONFLICT);
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMember.MemberRole.MEMBER)
                .build();
        groupMemberRepository.save(member);

        badgeService.awardFirstGroup(user);
        log.info("{} a rejoint le groupe {}", user.getEmail(), groupId);
    }

    // ── Feed ──────────────────────────────────────────────────────────────

    /**
     * Retourne les captures des membres du groupe, triées par date décroissante.
     */
    @Transactional(readOnly = true)
    public Page<FeedItemResponse> getGroupFeed(Long groupId, User requester, Pageable pageable) {
        // Vérifier que l'utilisateur est membre du groupe
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requester.getId())) {
            throw new BusinessException("Vous n'êtes pas membre de ce groupe", HttpStatus.FORBIDDEN);
        }

        // Récupérer les IDs des membres
        List<Long> memberIds = groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(gm -> gm.getUser().getId())
                .toList();

        if (memberIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Récupérer les captures de tous les membres (paginées, triées par date)
        org.springframework.data.jpa.domain.Specification<Capture> spec =
                (root, query, cb) -> {
                    query.orderBy(cb.desc(root.get("caughtAt")));
                    return root.get("user").get("id").in(memberIds);
                };

        Page<Capture> captures = captureRepository.findAll(spec, pageable);
        return captures.map(FeedItemResponse::from);
    }
}
