package com.fishdex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fishdex.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface LeaderboardRepository extends JpaRepository<User, Long> {

    /**
     * Classement par nombre de captures.
     * Retourne (userId, username, userTag, totalCaptures, totalWeight, distinctSpecies)
     */
    @Query("""
        SELECT u.id, u.username, u.userTag,
               COUNT(c.id),
               COALESCE(SUM(c.weight), 0),
               COUNT(DISTINCT c.speciesName)
        FROM User u
        LEFT JOIN Capture c ON c.user.id = u.id AND c.caughtAt >= :since
        GROUP BY u.id, u.username, u.userTag
        ORDER BY COUNT(c.id) DESC
        LIMIT 20
        """)
    List<Object[]> rankByCaptures(@Param("since") LocalDateTime since);

    /**
     * Classement par poids total.
     */
    @Query("""
        SELECT u.id, u.username, u.userTag,
               COUNT(c.id),
               COALESCE(SUM(c.weight), 0),
               COUNT(DISTINCT c.speciesName)
        FROM User u
        LEFT JOIN Capture c ON c.user.id = u.id AND c.caughtAt >= :since
        GROUP BY u.id, u.username, u.userTag
        ORDER BY COALESCE(SUM(c.weight), 0) DESC
        LIMIT 20
        """)
    List<Object[]> rankByWeight(@Param("since") LocalDateTime since);

    /**
     * Classement par diversité d'espèces.
     */
    @Query("""
        SELECT u.id, u.username, u.userTag,
               COUNT(c.id),
               COALESCE(SUM(c.weight), 0),
               COUNT(DISTINCT c.speciesName)
        FROM User u
        LEFT JOIN Capture c ON c.user.id = u.id AND c.caughtAt >= :since
        GROUP BY u.id, u.username, u.userTag
        ORDER BY COUNT(DISTINCT c.speciesName) DESC
        LIMIT 20
        """)
    List<Object[]> rankBySpecies(@Param("since") LocalDateTime since);
}
