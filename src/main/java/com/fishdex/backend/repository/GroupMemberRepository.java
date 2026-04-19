package com.fishdex.backend.repository;

import com.fishdex.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    @Query("SELECT gm.user.id FROM GroupMember gm WHERE gm.group.id = :groupId")
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId);
}
