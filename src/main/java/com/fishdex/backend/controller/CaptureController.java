package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.CaptureResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.service.CaptureService;
import com.fishdex.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
public class CaptureController {

    private final CaptureService captureService;
    private final UserService userService;

    /** POST /api/captures */
    @PostMapping
    public ResponseEntity<ApiResponse<CaptureResponse>> createCapture(
            @Valid @RequestBody CaptureRequest request,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.createCapture(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Capture ajoutée avec succès", response));
    }

    /**
     * GET /api/captures?page=0&size=12&speciesId=1&from=2025-01-01&to=2025-12-31
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CaptureResponse>>> getMyCaptures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long speciesId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "caughtAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        Page<CaptureResponse> captures = captureService.getMyCaptures(
                user, speciesId, from, to, sortBy, sortDir, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(captures));
    }

    /** GET /api/captures/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaptureResponse>> getCaptureById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(captureService.getCaptureById(id, user)));
    }

    /** PUT /api/captures/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaptureResponse>> updateCapture(
            @PathVariable Long id,
            @Valid @RequestBody CaptureRequest request,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.updateCapture(id, request, user);
        return ResponseEntity.ok(ApiResponse.ok("Capture mise à jour", response));
    }

    /** DELETE /api/captures/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapture(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        captureService.deleteCapture(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Photos ────────────────────────────────────────────────────────────

    /**
     * POST /api/captures/{id}/photo
     * Frontend envoie un FormData avec le champ "photo" (MultipartFile).
     */
    @PostMapping("/{id}/photo")
    public ResponseEntity<ApiResponse<CaptureResponse>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile file,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.uploadPhoto(id, file, user);
        return ResponseEntity.ok(ApiResponse.ok("Photo mise à jour", response));
    }

    /**
     * DELETE /api/captures/{id}/photo
     * Supprime la photo d'une capture.
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable Long id,
            Authentication authentication) {
        User user = userService.loadUserByEmail(authentication.getName());
        captureService.deletePhoto(id, user);
        return ResponseEntity.noContent().build();
    }
}
