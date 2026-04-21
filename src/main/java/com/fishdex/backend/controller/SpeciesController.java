package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.service.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

    /**
     * GET /api/species?search=brochet&page=0&size=20
     * Route publique — accessible sans JWT.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SpeciesResponse>>> getSpecies(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpeciesResponse> result = speciesService.getSpecies(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/species/{id}
     * Route publique — fiche détaillée d'une espèce.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpeciesResponse>> getSpeciesById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(speciesService.getSpeciesById(id)));
    }
}
