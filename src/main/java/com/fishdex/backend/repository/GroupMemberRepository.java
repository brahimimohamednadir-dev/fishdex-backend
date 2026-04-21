package com.fishdex.backend.repository;

import com.fishdex.backend.entity.GroupMember;
import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    long countByUserId(Long userId);

    List<GroupMember> findByUser(User user);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId ORDER BY gm.joinedAt ASC")
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);
}
