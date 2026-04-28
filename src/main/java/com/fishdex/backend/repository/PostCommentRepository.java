package com.fishdex.backend.repository;

import com.fishdex.backend.entity.PostComment;
import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<PostComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    @Modifying
    @Query("DELETE FROM PostComment c WHERE c.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
