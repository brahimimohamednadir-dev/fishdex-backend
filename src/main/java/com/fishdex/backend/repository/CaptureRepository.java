package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Capture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaptureRepository extends JpaRepository<Capture, Long>,
        JpaSpecificationExecutor<Capture> {

    Page<Capture> findByUserIdOrderByCaughtAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    // ── Statistiques ─────────────────────────────────────────────────────

    @Query("SELECT MAX(c.weight) FROM Capture c WHERE c.user.id = :userId")
    Double findMaxWeightByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(c.weight) FROM Capture c WHERE c.user.id = :userId")
    Double findTotalWeightByUserId(@Param("userId") Long userId);

    /** La capture avec le poids maximal */
    @Query("SELECT c FROM Capture c WHERE c.user.id = :userId AND c.weight = " +
           "(SELECT MAX(c2.weight) FROM Capture c2 WHERE c2.user.id = :userId)")
    Optional<Capture> findHeaviestByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(c.length) FROM Capture c WHERE c.user.id = :userId")
    Double findMaxLengthByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(c.caughtAt) FROM Capture c WHERE c.user.id = :userId")
    LocalDateTime findFirstCaptureDateByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(c.caughtAt) FROM Capture c WHERE c.user.id = :userId")
    LocalDateTime findLastCaptureDateByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Capture c WHERE c.user.id = :userId AND c.caughtAt >= :since")
    long countByUserIdAndCaughtAtAfter(@Param("userId") Long userId,
                                       @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT c.species.id) FROM Capture c " +
           "WHERE c.user.id = :userId AND c.species IS NOT NULL")
    long countDistinctSpeciesByUserId(@Param("userId") Long userId);

    /** Retourne { speciesName, count } triés par count DESC */
    @Query("SELECT c.speciesName, COUNT(c) as cnt FROM Capture c " +
           "WHERE c.user.id = :userId " +
           "GROUP BY c.speciesName ORDER BY cnt DESC")
    List<Object[]> findMostCaughtSpeciesByUserId(@Param("userId") Long userId, Pageable pageable);

    /** Retourne { speciesName, count } — toutes les espèces pour la map */
    @Query("SELECT c.speciesName, COUNT(c) as cnt FROM Capture c " +
           "WHERE c.user.id = :userId GROUP BY c.speciesName")
    List<Object[]> findCapturesBySpeciesNameForUser(@Param("userId") Long userId);

    /** Retourne year + month séparément pour compatibilité H2/MySQL */
    @Query("SELECT YEAR(c.caughtAt), MONTH(c.caughtAt), COUNT(c) as cnt " +
           "FROM Capture c WHERE c.user.id = :userId " +
           "GROUP BY YEAR(c.caughtAt), MONTH(c.caughtAt) ORDER BY cnt DESC")
    List<Object[]> findCapturesByMonthForUser(@Param("userId") Long userId, Pageable pageable);

    // ── Feed social ──────────────────────────────────────────────────────

    // ── Profil public ────────────────────────────────────────────────────

    /** Captures publiques d'un utilisateur (pour la grille profil) */
    Page<Capture> findByUserIdAndVisibilityOrderByCreatedAtDesc(
            Long userId, Capture.Visibility visibility, Pageable pageable);

    /** Record le plus lourd par userId */
    @Query("SELECT c FROM Capture c WHERE c.user.id = :userId ORDER BY c.weight DESC")
    List<Capture> findTopByWeightForUser(@Param("userId") Long userId, Pageable pageable);

    // ── Stats personnelles ────────────────────────────────────────────────

    /** Captures par mois pour une année donnée : (month, count) */
    @Query("SELECT MONTH(c.caughtAt), COUNT(c) FROM Capture c " +
           "WHERE c.user.id = :userId AND YEAR(c.caughtAt) = :year " +
           "GROUP BY MONTH(c.caughtAt)")
    List<Object[]> findMonthlyCaptureCountsForYear(@Param("userId") Long userId,
                                                    @Param("year") int year);

    /** Records par espèce : (speciesName, count, maxWeight, maxLength) */
    @Query("SELECT c.speciesName, COUNT(c), MAX(c.weight), MAX(c.length) FROM Capture c " +
           "WHERE c.user.id = :userId " +
           "GROUP BY c.speciesName ORDER BY COUNT(c) DESC")
    List<Object[]> findSpeciesRecordsForUser(@Param("userId") Long userId, Pageable pageable);

    /** Spots GPS groupés (lat/lng arrondis à 2 décimales) : (lat, lng, count) */
    @Query("SELECT ROUND(c.latitude, 2), ROUND(c.longitude, 2), COUNT(c) FROM Capture c " +
           "WHERE c.user.id = :userId AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL " +
           "GROUP BY ROUND(c.latitude, 2), ROUND(c.longitude, 2) ORDER BY COUNT(c) DESC")
    List<Object[]> findFavoriteSpotsForUser(@Param("userId") Long userId, Pageable pageable);

    /** Dernière capture d'un utilisateur (pour l'aperçu ami) */
    Optional<Capture> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /** Feed : captures des amis (visibility PUBLIC ou FRIENDS) + propres captures */
    @Query("SELECT c FROM Capture c WHERE " +
           "(c.user.id IN :friendIds AND c.visibility IN ('PUBLIC', 'FRIENDS')) " +
           "OR (c.user.id = :myId) " +
           "ORDER BY c.createdAt DESC")
    Page<Capture> findFeedCaptures(@Param("friendIds") List<Long> friendIds,
                                   @Param("myId") Long myId,
                                   Pageable pageable);

    /** Feed public (pour les utilisateurs sans amis ou pour la découverte) */
    @Query("SELECT c FROM Capture c WHERE c.visibility = 'PUBLIC' ORDER BY c.createdAt DESC")
    Page<Capture> findPublicCaptures(Pageable pageable);

    /** Nombre de nouvelles captures dans le feed depuis une date */
    @Query("SELECT COUNT(c) FROM Capture c WHERE " +
           "((c.user.id IN :friendIds AND c.visibility IN ('PUBLIC', 'FRIENDS')) " +
           "OR c.user.id = :myId) " +
           "AND c.createdAt > :since")
    long countNewFeedCaptures(@Param("friendIds") List<Long> friendIds,
                              @Param("myId") Long myId,
                              @Param("since") LocalDateTime since);
}
