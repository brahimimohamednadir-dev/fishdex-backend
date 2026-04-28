package com.fishdex.backend.service;

import com.fishdex.backend.dto.GroupMemberResponse;
import com.fishdex.backend.dto.GroupResponse;
import com.fishdex.backend.dto.JoinRequestResponse;
import com.fishdex.backend.dto.ReportResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository joinRequestRepository;
    private final CaptureRepository captureRepository;
    private final PostReportRepository reportRepository;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    // ── Mes groupes ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(User user) {
        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        return memberships.stream()
                .map(m -> toResponse(m.getGroup(), user))
                .collect(Collectors.toList());
    }

    // ── Découverte ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<GroupResponse> discoverGroups(String search, String category, Pageable pageable, User user) {
        String cat = (category == null || category.isBlank()) ? null : category;
        return groupRepository.discoverGroups(search, cat, pageable)
                .map(g -> toResponse(g, user));
    }

    // ── Créer ─────────────────────────────────────────────────────────────

    @Transactional
    public GroupResponse createGroup(String name, String description,
                                     String visibility, String category,
                                     String rules, String coverPhotoUrl, User creator) {
        Group.GroupVisibility vis = parseEnum(Group.GroupVisibility.class, visibility, "visibility");
        Group.GroupCategory cat = parseEnum(Group.GroupCategory.class, category, "category");

        Group group = Group.builder()
                .name(name.trim())
                .description(description)
                .visibility(vis)
                .category(cat)
                .rules(rules)
                .coverPhotoUrl(coverPhotoUrl)
                .creator(creator)
                .build();
        group = groupRepository.save(group);

        GroupMember membership = GroupMember.builder()
                .group(group).user(creator).role(GroupMember.MemberRole.OWNER).build();
        groupMemberRepository.save(membership);

        badgeService.awardFirstGroup(creator);
        log.info("Groupe '{}' créé par {}", name, creator.getEmail());
        return toResponse(group, creator);
    }

    // ── Détails ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id, User user) {
        Group group = loadGroup(id);
        return toResponse(group, user);
    }

    // ── Supprimer ─────────────────────────────────────────────────────────

    @Transactional
    public void deleteGroup(Long id, User user) {
        Group group = loadGroup(id);
        requireOwner(id, user);
        groupRepository.delete(group);
        log.info("Groupe {} supprimé par {}", id, user.getEmail());
    }

    // ── Rejoindre ─────────────────────────────────────────────────────────

    @Transactional
    public GroupResponse joinGroup(Long groupId, String message, User user) {
        Group group = loadGroup(groupId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("Vous êtes déjà membre de ce groupe", HttpStatus.CONFLICT);
        }

        if (group.getVisibility() == Group.GroupVisibility.PUBLIC) {
            // Adhésion directe
            GroupMember m = GroupMember.builder()
                    .group(group).user(user).role(GroupMember.MemberRole.MEMBER).build();
            groupMemberRepository.save(m);
            badgeService.awardFirstGroup(user);
            log.info("{} a rejoint le groupe {}", user.getEmail(), groupId);
        } else {
            // PRIVATE / SECRET → demande d'adhésion
            Optional<GroupJoinRequest> existing = joinRequestRepository.findByGroupIdAndUserId(groupId, user.getId());
            if (existing.isPresent() && existing.get().getStatus() == GroupJoinRequest.RequestStatus.PENDING) {
                throw new BusinessException("Une demande est déjà en attente", HttpStatus.CONFLICT);
            }
            GroupJoinRequest request = GroupJoinRequest.builder()
                    .group(group).user(user).message(message).build();
            joinRequestRepository.save(request);
            log.info("{} a demandé à rejoindre le groupe {}", user.getEmail(), groupId);
        }
        return toResponse(group, user);
    }

    // ── Quitter ───────────────────────────────────────────────────────────

    @Transactional
    public void leaveGroup(Long groupId, User user) {
        GroupMember m = groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new BusinessException("Vous n'êtes pas membre de ce groupe", HttpStatus.NOT_FOUND));
        if (m.getRole() == GroupMember.MemberRole.OWNER) {
            throw new BusinessException("Le propriétaire ne peut pas quitter le groupe sans transférer la propriété", HttpStatus.BAD_REQUEST);
        }
        groupMemberRepository.delete(m);
    }

    // ── Membres ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<GroupMemberResponse> getMembers(Long groupId, String search, Pageable pageable) {
        loadGroup(groupId);
        return groupMemberRepository.findByGroupIdWithSearch(groupId, search, pageable)
                .map(m -> {
                    long captureCount = captureRepository.countByUserId(m.getUser().getId());
                    return GroupMemberResponse.from(m, captureCount);
                });
    }

    @Transactional
    public void changeMemberRole(Long groupId, Long userId, String role, User requester) {
        requireAdminOrOwner(groupId, requester);
        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException("Membre introuvable", HttpStatus.NOT_FOUND));
        GroupMember.MemberRole newRole = parseEnum(GroupMember.MemberRole.class, role, "role");
        if (newRole == GroupMember.MemberRole.OWNER) {
            throw new BusinessException("Impossible d'assigner le rôle OWNER via cette route", HttpStatus.BAD_REQUEST);
        }
        target.setRole(newRole);
        groupMemberRepository.save(target);
    }

    @Transactional
    public void kickMember(Long groupId, Long userId, User requester) {
        requireAdminOrOwner(groupId, requester);
        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException("Membre introuvable", HttpStatus.NOT_FOUND));
        if (target.getRole() == GroupMember.MemberRole.OWNER) {
            throw new BusinessException("Impossible d'exclure le propriétaire", HttpStatus.BAD_REQUEST);
        }
        Group group = loadGroup(groupId);
        notificationService.create(
                target.getUser(), Notification.NotificationType.GROUP_KICKED,
                requester.getUsername(), group.getName(), groupId, null);
        groupMemberRepository.delete(target);
    }

    // ── Demandes d'adhésion ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getJoinRequests(Long groupId, User requester) {
        requireAdminOrOwner(groupId, requester);
        return joinRequestRepository.findByGroupIdAndStatus(groupId, GroupJoinRequest.RequestStatus.PENDING)
                .stream().map(JoinRequestResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void acceptRequest(Long groupId, Long userId, User requester) {
        requireAdminOrOwner(groupId, requester);
        GroupJoinRequest req = loadPendingRequest(groupId, userId);
        Group group = loadGroup(groupId);

        req.setStatus(GroupJoinRequest.RequestStatus.ACCEPTED);
        joinRequestRepository.save(req);

        GroupMember m = GroupMember.builder()
                .group(group).user(req.getUser()).role(GroupMember.MemberRole.MEMBER).build();
        groupMemberRepository.save(m);

        notificationService.create(
                req.getUser(), Notification.NotificationType.JOIN_REQUEST_ACCEPTED,
                requester.getUsername(), group.getName(), groupId, null);
    }

    @Transactional
    public void rejectRequest(Long groupId, Long userId, User requester) {
        requireAdminOrOwner(groupId, requester);
        GroupJoinRequest req = loadPendingRequest(groupId, userId);
        Group group = loadGroup(groupId);

        req.setStatus(GroupJoinRequest.RequestStatus.REJECTED);
        joinRequestRepository.save(req);

        notificationService.create(
                req.getUser(), Notification.NotificationType.JOIN_REQUEST_REJECTED,
                requester.getUsername(), group.getName(), groupId, null);
    }

    // ── Signalements ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(Long groupId, User requester) {
        requireAdminOrOwner(groupId, requester);
        return reportRepository.findByPostGroupId(groupId).stream()
                .map(ReportResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void deleteReport(Long groupId, Long reportId, User requester) {
        requireAdminOrOwner(groupId, requester);
        reportRepository.findById(reportId)
                .ifPresent(reportRepository::delete);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private GroupResponse toResponse(Group group, User user) {
        long memberCount = groupMemberRepository.countByGroupId(group.getId());
        GroupMember myMembership = groupMemberRepository
                .findByGroupIdAndUserId(group.getId(), user.getId()).orElse(null);
        GroupJoinRequest myRequest = joinRequestRepository
                .findByGroupIdAndUserId(group.getId(), user.getId()).orElse(null);
        return GroupResponse.from(group, memberCount, myMembership, myRequest);
    }

    private Group loadGroup(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Groupe introuvable", HttpStatus.NOT_FOUND));
    }

    private GroupJoinRequest loadPendingRequest(Long groupId, Long userId) {
        return joinRequestRepository.findByGroupIdAndUserIdAndStatus(
                        groupId, userId, GroupJoinRequest.RequestStatus.PENDING)
                .orElseThrow(() -> new BusinessException("Demande introuvable", HttpStatus.NOT_FOUND));
    }

    private void requireOwner(Long groupId, User user) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .filter(m -> m.getRole() == GroupMember.MemberRole.OWNER)
                .orElseThrow(() -> new BusinessException("Action réservée au propriétaire", HttpStatus.FORBIDDEN));
    }

    private void requireAdminOrOwner(Long groupId, User user) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .filter(m -> m.getRole() == GroupMember.MemberRole.OWNER
                        || m.getRole() == GroupMember.MemberRole.ADMIN)
                .orElseThrow(() -> new BusinessException("Action réservée aux administrateurs", HttpStatus.FORBIDDEN));
    }

    private <E extends Enum<E>> E parseEnum(Class<E> clazz, String value, String field) {
        try {
            return Enum.valueOf(clazz, value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException("Valeur invalide pour " + field + " : " + value, HttpStatus.BAD_REQUEST);
        }
    }
}
