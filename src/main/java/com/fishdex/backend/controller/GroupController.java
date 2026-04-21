package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.FeedItemResponse;
import com.fishdex.backend.dto.GroupResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.service.GroupService;
import com.fishdex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints groupes — correspond aux appels du frontend Angular (GroupService).
 *
 * POST   /api/groups              — createGroup
 * GET    /api/groups/{id}         — getGroupById
 * POST   /api/groups/{id}/join    — joinGroup
 * GET    /api/groups/{id}/feed    — getGroupFeed
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    /** POST /api/groups — Créer un groupe */
    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @RequestBody GroupRequest request,
            Authentication authentication) {

        User user = userService.loadUserByEmail(authentication.getName());
        GroupResponse response = groupService.createGroup(
                request.name(), request.description(), request.type(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Groupe créé", response));
    }

    /** GET /api/groups/{id} — Détails d'un groupe */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroupById(id)));
    }

    /** POST /api/groups/{id}/join — Rejoindre un groupe */
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> joinGroup(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userService.loadUserByEmail(authentication.getName());
        groupService.joinGroup(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Vous avez rejoint le groupe", null));
    }

    /** GET /api/groups/{id}/feed?page=0&size=20 — Fil d'activité du groupe */
    @GetMapping("/{id}/feed")
    public ResponseEntity<ApiResponse<Page<FeedItemResponse>>> getGroupFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User user = userService.loadUserByEmail(authentication.getName());
        Page<FeedItemResponse> feed = groupService.getGroupFeed(id, user, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(feed));
    }

    // ── Inner record ──────────────────────────────────────────────────────

    /** { name, description?, type } — correspond à GroupRequest frontend */
    record GroupRequest(String name, String description, String type) {}
}
