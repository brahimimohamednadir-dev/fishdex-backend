package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Species;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

    Page<Species> findByCommonNameContainingIgnoreCase(String commonName, Pageable pageable);
}
