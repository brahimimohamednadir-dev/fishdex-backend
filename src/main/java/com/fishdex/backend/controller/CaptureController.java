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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
public class CaptureController {

    private final CaptureService captureService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CaptureResponse>> createCapture(
            @Valid @RequestBody CaptureRequest request,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.createCapture(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Capture ajoutée avec succès", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CaptureResponse>>> getMyCaptures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<CaptureResponse> captures = captureService.getMyCaptures(user, pageable);
        return ResponseEntity.ok(ApiResponse.ok(captures));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaptureResponse>> getCaptureById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.getCaptureById(id, user);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaptureResponse>> updateCapture(
            @PathVariable Long id,
            @Valid @RequestBody CaptureRequest request,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        CaptureResponse response = captureService.updateCapture(id, request, user);
        return ResponseEntity.ok(ApiResponse.ok("Capture mise à jour", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapture(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        captureService.deleteCapture(id, user);
        return ResponseEntity.noContent().build();
    }
}
