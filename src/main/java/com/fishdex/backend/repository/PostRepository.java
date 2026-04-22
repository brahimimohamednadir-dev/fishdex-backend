package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByGroupIdOrderByPinnedDescCreatedAtDesc(Long groupId, Pageable pageable);

    long countByGroupId(Long groupId);
}
