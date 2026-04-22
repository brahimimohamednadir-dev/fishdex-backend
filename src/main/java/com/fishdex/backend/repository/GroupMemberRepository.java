package com.fishdex.backend.repository;

import com.fishdex.backend.entity.GroupMember;
import com.fishdex.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    long countByUserId(Long userId);

    List<GroupMember> findByUser(User user);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId ORDER BY gm.joinedAt ASC")
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gm FROM GroupMember gm JOIN gm.user u WHERE gm.group.id = :groupId " +
            "AND (:search IS NULL OR :search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<GroupMember> findByGroupIdWithSearch(@Param("groupId") Long groupId,
                                              @Param("search") String search,
                                              Pageable pageable);

    @Query("SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId")
    List<Long> findGroupIdsByUserId(@Param("userId") Long userId);
}
