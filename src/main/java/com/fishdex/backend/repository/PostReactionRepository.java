package com.fishdex.backend.repository;

import com.fishdex.backend.entity.PostReaction;
import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    List<PostReaction> findByPostId(Long postId);

    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT r.type, COUNT(r) FROM PostReaction r WHERE r.post.id = :postId GROUP BY r.type")
    List<Object[]> countByTypeForPost(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM PostReaction r WHERE r.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
