package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

    /** Recherche par nom commun OU nom latin (insensible à la casse) */
    @Query("SELECT s FROM Species s WHERE " +
           "LOWER(s.commonName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.latinName)  LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.family)     LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Species> searchByNameOrFamily(@Param("search") String search, Pageable pageable);
}
