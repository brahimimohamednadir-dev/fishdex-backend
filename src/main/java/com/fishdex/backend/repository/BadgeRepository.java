package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Badge;
import com.fishdex.backend.entity.Badge.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    boolean existsByUserIdAndType(Long userId, BadgeType type);

    List<Badge> findByUserIdOrderByEarnedAtDesc(Long userId);
}
