package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Post;
import com.fishdex.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByGroupIdOrderByPinnedDescCreatedAtDesc(Long groupId, Pageable pageable);

    long countByGroupId(Long groupId);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
