package com.fishdex.backend.repository;

import com.fishdex.backend.entity.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {

    List<GroupJoinRequest> findByGroupIdAndStatus(Long groupId, GroupJoinRequest.RequestStatus status);

    Optional<GroupJoinRequest> findByGroupIdAndUserIdAndStatus(Long groupId, Long userId, GroupJoinRequest.RequestStatus status);

    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, GroupJoinRequest.RequestStatus status);

    Optional<GroupJoinRequest> findByGroupIdAndUserId(Long groupId, Long userId);
}
