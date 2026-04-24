package com.fishdex.backend.repository;

import com.fishdex.backend.entity.SpeciesTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeciesTipRepository extends JpaRepository<SpeciesTip, Long> {

    List<SpeciesTip> findBySpeciesIdOrderByUpvotesDescCreatedAtDesc(Long speciesId);
}
