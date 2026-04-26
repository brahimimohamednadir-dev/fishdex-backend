package com.fishdex.backend.repository;

import com.fishdex.backend.entity.CaptureReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaptureReactionRepository extends JpaRepository<CaptureReaction, Long> {

    Optional<CaptureReaction> findByCaptureIdAndUserId(Long captureId, Long userId);

    boolean existsByCaptureIdAndUserId(Long captureId, Long userId);

    int countByCaptureId(Long captureId);

    @Query("SELECT COUNT(r) FROM CaptureReaction r WHERE r.capture.id IN :captureIds")
    long countByCaptureIds(@Param("captureIds") java.util.List<Long> captureIds);
}
