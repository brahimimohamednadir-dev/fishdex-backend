package com.fishdex.backend.repository;

import com.fishdex.backend.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<PostComment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
