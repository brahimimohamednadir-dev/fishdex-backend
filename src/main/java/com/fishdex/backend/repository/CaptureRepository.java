package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Capture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaptureRepository extends JpaRepository<Capture, Long> {

    List<Capture> findByUserId(Long userId);

    Page<Capture> findByUserIdOrderByCaughtAtDesc(Long userId, Pageable pageable);

    Page<Capture> findByUserIdInOrderByCaughtAtDesc(List<Long> userIds, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndPhotoUrlIsNotNull(Long userId);

    Optional<Capture> findTopByUserIdOrderByWeightDesc(Long userId);
}
