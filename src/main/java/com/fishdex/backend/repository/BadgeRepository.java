package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    boolean existsByUserIdAndType(Long userId, Badge.BadgeType type);

    List<Badge> findByUserId(Long userId);
}
