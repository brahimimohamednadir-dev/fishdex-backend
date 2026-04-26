package com.fishdex.backend.repository;

import com.fishdex.backend.entity.CaptureComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaptureCommentRepository extends JpaRepository<CaptureComment, Long> {

    List<CaptureComment> findByCaptureIdOrderByCreatedAtAsc(Long captureId);

    /** Derniers commentaires d'une capture (pour la preview dans le feed) */
    @Query("SELECT c FROM CaptureComment c WHERE c.capture.id = :captureId ORDER BY c.createdAt DESC")
    List<CaptureComment> findTopByCaptureId(@Param("captureId") Long captureId,
                                             org.springframework.data.domain.Pageable pageable);

    int countByCaptureId(Long captureId);
}
