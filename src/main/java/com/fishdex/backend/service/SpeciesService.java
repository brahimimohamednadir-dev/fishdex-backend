package com.fishdex.backend.service;

import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.entity.Species;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    @Transactional(readOnly = true)
    public Page<SpeciesResponse> getSpecies(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return speciesRepository
                    .findByCommonNameContainingIgnoreCase(search.trim(), pageable)
                    .map(SpeciesResponse::from);
        }
        return speciesRepository.findAll(pageable).map(SpeciesResponse::from);
    }

    @Transactional(readOnly = true)
    public SpeciesResponse getSpeciesById(Long id) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Espèce introuvable", HttpStatus.NOT_FOUND));
        return SpeciesResponse.from(species);
    }
}
