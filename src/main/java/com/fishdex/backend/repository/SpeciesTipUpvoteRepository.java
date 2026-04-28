package com.fishdex.backend.repository;

import com.fishdex.backend.entity.SpeciesTipUpvote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpeciesTipUpvoteRepository extends JpaRepository<SpeciesTipUpvote, Long> {
    boolean existsByTipIdAndUserId(Long tipId, Long userId);
    Optional<SpeciesTipUpvote> findByTipIdAndUserId(Long tipId, Long userId);
}
