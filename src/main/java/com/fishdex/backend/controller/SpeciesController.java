package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.service.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

    /** GET /api/species?search=brochet&page=0&size=20 — Route publique */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SpeciesResponse>>> getSpecies(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(speciesService.getSpecies(search, pageable, null)));
    }

    /** GET /api/species/{id} — Fiche détaillée avec communityTips */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpeciesResponse>> getSpeciesById(
            @PathVariable Long id,
            Authentication auth) {
        String email = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(ApiResponse.ok(speciesService.getSpeciesById(id, email)));
    }

    /** POST /api/species/{speciesId}/tips — Ajouter un conseil communautaire */
    @PostMapping("/{speciesId}/tips")
    public ResponseEntity<ApiResponse<Void>> addTip(
            @PathVariable Long speciesId,
            @RequestBody TipBody body,
            Authentication auth) {
        speciesService.addTip(speciesId, body.content(), auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Conseil ajouté", null));
    }

    /** POST /api/species/tips/{tipId}/upvote — Upvoter un conseil (toggle) */
    @PostMapping("/tips/{tipId}/upvote")
    public ResponseEntity<ApiResponse<Void>> upvoteTip(
            @PathVariable Long tipId,
            Authentication auth) {
        speciesService.upvoteTip(tipId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Vote mis à jour", null));
    }

    record TipBody(String content) {}
}
