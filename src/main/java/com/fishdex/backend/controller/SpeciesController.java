package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.SpeciesResponse;
import com.fishdex.backend.service.SpeciesService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

    // ── Liste ─────────────────────────────────────────────────────────────

    /**
     * GET /api/species?search=brochet&page=0&size=20
     * Route publique — accessible sans JWT.
     * Si JWT présent, enrichit isCaught pour chaque espèce.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SpeciesResponse>>> getSpecies(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String email = auth != null ? auth.getName() : null;
        Page<SpeciesResponse> result = speciesService.getSpecies(search, pageable, email);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── Détail ────────────────────────────────────────────────────────────

    /**
     * GET /api/species/{id}
     * Route publique — fiche détaillée avec tips, stats communauté.
     * Si JWT présent, enrichit isCaught + personalStats.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpeciesResponse>> getSpeciesById(
            @PathVariable Long id,
            Authentication auth
    ) {
        String email = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(ApiResponse.ok(speciesService.getSpeciesById(id, email)));
    }

    // ── Community Tips ────────────────────────────────────────────────────

    /**
     * POST /api/species/{speciesId}/tips
     * Ajouter un conseil communautaire — JWT requis.
     */
    @PostMapping("/{speciesId}/tips")
    public ResponseEntity<ApiResponse<SpeciesResponse.CommunityTipDto>> addTip(
            @PathVariable Long speciesId,
            @RequestBody TipRequest body,
            Authentication auth
    ) {
        SpeciesResponse.CommunityTipDto tip = speciesService.addTip(speciesId, body.content(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(tip));
    }

    /**
     * POST /api/species/tips/{tipId}/upvote
     * Toggle upvote sur un conseil — JWT requis.
     */
    @PostMapping("/tips/{tipId}/upvote")
    public ResponseEntity<ApiResponse<SpeciesResponse.CommunityTipDto>> upvoteTip(
            @PathVariable Long tipId,
            Authentication auth
    ) {
        SpeciesResponse.CommunityTipDto tip = speciesService.upvoteTip(tipId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(tip));
    }

    // ── DTO interne ───────────────────────────────────────────────────────

    record TipRequest(@NotBlank @Size(max = 500) String content) {}
}
