package com.fishdex.backend.service;

import com.fishdex.backend.dto.FeedItemResponse;
import com.fishdex.backend.dto.GroupRequest;
import com.fishdex.backend.dto.GroupResponse;
import com.fishdex.backend.entity.Group;
import com.fishdex.backend.entity.GroupMember;
import com.fishdex.backend.entity.GroupMember.MemberRole;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CaptureRepository captureRepository;
    private final BadgeService badgeService;

    @Transactional
    public GroupResponse createGroup(GroupRequest request, User creator) {
        if (groupRepository.existsByName(request.getName())) {
            throw new BusinessException("Un groupe avec ce nom existe déjà", HttpStatus.CONFLICT);
        }

        Group group = Group.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .type(request.getType())
                .creator(creator)
                .build();

        Group saved = groupRepository.save(group);

        GroupMember adminMember = GroupMember.builder()
                .group(saved)
                .user(creator)
                .role(MemberRole.ADMIN)
                .build();
        groupMemberRepository.save(adminMember);

        return GroupResponse.from(saved, 1L);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long id) {
        Group group = findGroup(id);
        long memberCount = groupMemberRepository.countByGroupId(id);
        return GroupResponse.from(group, memberCount);
    }

    @Transactional
    public void joinGroup(Long groupId, User user) {
        Group group = findGroup(groupId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("Vous êtes déjà membre de ce groupe", HttpStatus.CONFLICT);
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(MemberRole.MEMBER)
                .build();
        groupMemberRepository.save(member);

        badgeService.awardFirstGroup(user);
    }

    @Transactional(readOnly = true)
    public Page<FeedItemResponse> getFeed(Long groupId, User user, Pageable pageable) {
        findGroup(groupId);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessException("Vous devez être membre du groupe pour voir le fil", HttpStatus.FORBIDDEN);
        }

        List<Long> memberIds = groupMemberRepository.findUserIdsByGroupId(groupId);
        return captureRepository.findByUserIdInOrderByCaughtAtDesc(memberIds, pageable)
                .map(FeedItemResponse::from);
    }

    private Group findGroup(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Groupe introuvable", HttpStatus.NOT_FOUND));
    }
}
