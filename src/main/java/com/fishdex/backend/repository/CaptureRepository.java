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

    /** 5 captures les plus récentes d'un utilisateur (profil public) */
    List<Capture> findTop5ByUserIdOrderByCaughtAtDesc(Long userId);

    /** Capture la plus longue d'un utilisateur */
    @Query("SELECT c FROM Capture c WHERE c.user.id = :userId AND c.length = " +
           "(SELECT MAX(c2.length) FROM Capture c2 WHERE c2.user.id = :userId)")
    List<Capture> findLongestByUserId(@Param("userId") Long userId, Pageable pageable);

    /** Espèces avec comptage, record poids et record longueur */
    @Query("SELECT c.speciesName, COUNT(c), MAX(c.weight), MAX(c.length) " +
           "FROM Capture c WHERE c.user.id = :userId " +
           "GROUP BY c.speciesName ORDER BY COUNT(c) DESC")
    List<Object[]> findTopSpeciesWithRecords(@Param("userId") Long userId, Pageable pageable);

    /** Spots favoris groupés par coordonnées arrondies (requête native MySQL) */
    @Query(value = "SELECT ROUND(latitude, 2) as lat, ROUND(longitude, 2) as lng, COUNT(*) as cnt " +
                   "FROM captures WHERE user_id = :userId AND latitude IS NOT NULL " +
                   "GROUP BY ROUND(latitude, 2), ROUND(longitude, 2) ORDER BY cnt DESC LIMIT 5",
           nativeQuery = true)
    List<Object[]> findFavoriteSpots(@Param("userId") Long userId);

    /** Captures mensuelles depuis une date donnée */
    @Query("SELECT YEAR(c.caughtAt), MONTH(c.caughtAt), COUNT(c) " +
           "FROM Capture c WHERE c.user.id = :userId AND c.caughtAt >= :since " +
           "GROUP BY YEAR(c.caughtAt), MONTH(c.caughtAt) ORDER BY YEAR(c.caughtAt) ASC, MONTH(c.caughtAt) ASC")
    List<Object[]> findMonthlyCaptures(@Param("userId") Long userId, @Param("since") LocalDateTime since);

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

    @Query("SELECT COUNT(DISTINCT c.speciesName) FROM Capture c WHERE c.user.id = :userId")
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

    // ── Par espèce (FK) — pour la fiche espèce ───────────────────────────

    boolean existsByUserIdAndSpeciesId(Long userId, Long speciesId);

    long countBySpeciesId(Long speciesId);

    long countByUserIdAndSpeciesId(Long userId, Long speciesId);

    @Query("SELECT AVG(c.weight) FROM Capture c WHERE c.user.id = :userId AND c.species.id = :speciesId")
    Double findAvgWeightByUserIdAndSpeciesId(@Param("userId") Long userId,
                                             @Param("speciesId") Long speciesId);

    @Query("SELECT MAX(c.caughtAt) FROM Capture c WHERE c.user.id = :userId AND c.species.id = :speciesId")
    LocalDateTime findLastCatchByUserIdAndSpeciesId(@Param("userId") Long userId,
                                                    @Param("speciesId") Long speciesId);

    @Query("SELECT COUNT(c) FROM Capture c WHERE c.user.id = :userId AND c.species.id = :speciesId " +
           "AND YEAR(c.caughtAt) = :year")
    long countByUserIdAndSpeciesIdAndYear(@Param("userId") Long userId,
                                          @Param("speciesId") Long speciesId,
                                          @Param("year") int year);

    /** Record personnel : capture la plus lourde pour cet utilisateur + espèce */
    @Query("SELECT c FROM Capture c WHERE c.user.id = :userId AND c.species.id = :speciesId " +
           "AND c.weight = (SELECT MAX(c2.weight) FROM Capture c2 WHERE c2.user.id = :userId AND c2.species.id = :speciesId) " +
           "ORDER BY c.caughtAt DESC")
    org.springframework.data.domain.Page<Capture> findPersonalRecordByUserIdAndSpeciesId(
            @Param("userId") Long userId,
            @Param("speciesId") Long speciesId,
            org.springframework.data.domain.Pageable pageable);

    /** Record FishDex : capture la plus lourde toutes espèces confondues */
    @Query("SELECT c FROM Capture c WHERE c.species.id = :speciesId " +
           "AND c.weight = (SELECT MAX(c2.weight) FROM Capture c2 WHERE c2.species.id = :speciesId) " +
           "ORDER BY c.caughtAt DESC")
    org.springframework.data.domain.Page<Capture> findFishDexRecordBySpeciesId(
            @Param("speciesId") Long speciesId,
            org.springframework.data.domain.Pageable pageable);
}
