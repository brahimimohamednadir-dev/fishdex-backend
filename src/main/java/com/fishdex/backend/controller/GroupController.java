package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    // ── Mes groupes ───────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups(Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(groupService.getMyGroups(user)));
    }

    // ── Découverte ────────────────────────────────────────────────────────

    @GetMapping("/discover")
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> discover(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        Page<GroupResponse> result = groupService.discoverGroups(search, category, PageRequest.of(page, size), user);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── Créer ─────────────────────────────────────────────────────────────

    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
            @RequestParam(value = "category", defaultValue = "FRIENDS") String category,
            @RequestParam(value = "rules", required = false) String rules,
            @RequestParam(value = "coverPhoto", required = false) MultipartFile coverPhoto,
            Authentication auth) {

        User user = userService.loadUserByEmail(auth.getName());
        // TODO: upload coverPhoto vers Cloudinary si fourni
        String coverPhotoUrl = null;
        GroupResponse response = groupService.createGroup(
                name, description, visibility, category, rules, coverPhotoUrl, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Groupe créé", response));
    }

    // ── Détails ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getById(
            @PathVariable Long id, Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroupById(id, user)));
    }

    // ── Supprimer ─────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        groupService.deleteGroup(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Groupe supprimé", null));
    }

    // ── Rejoindre ─────────────────────────────────────────────────────────

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<GroupResponse>> join(
            @PathVariable Long id,
            @RequestBody(required = false) JoinBody body,
            Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        String message = body != null ? body.message() : null;
        GroupResponse response = groupService.joinGroup(id, message, user);
        return ResponseEntity.ok(ApiResponse.ok("Demande envoyée", response));
    }

    // ── Quitter ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leave(
            @PathVariable Long id, Authentication auth) {
        User user = userService.loadUserByEmail(auth.getName());
        groupService.leaveGroup(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Vous avez quitté le groupe", null));
    }

    // ── Membres ───────────────────────────────────────────────────────────

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Page<GroupMemberResponse>>> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GroupMemberResponse> members = groupService.getMembers(id, search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(members));
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody RoleBody body,
            Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        groupService.changeMemberRole(id, userId, body.role(), requester);
        return ResponseEntity.ok(ApiResponse.ok("Rôle mis à jour", null));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        groupService.kickMember(id, userId, requester);
        return ResponseEntity.ok(ApiResponse.ok("Membre exclu", null));
    }

    // ── Demandes d'adhésion ───────────────────────────────────────────────

    @GetMapping("/{id}/requests")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> getRequests(
            @PathVariable Long id, Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(groupService.getJoinRequests(id, requester)));
    }

    @PostMapping("/{id}/requests/{userId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        groupService.acceptRequest(id, userId, requester);
        return ResponseEntity.ok(ApiResponse.ok("Demande acceptée", null));
    }

    @PostMapping("/{id}/requests/{userId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        groupService.rejectRequest(id, userId, requester);
        return ResponseEntity.ok(ApiResponse.ok("Demande refusée", null));
    }

    // ── Signalements ──────────────────────────────────────────────────────

    @GetMapping("/{id}/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @PathVariable Long id, Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(groupService.getReports(id, requester)));
    }

    @DeleteMapping("/{id}/reports/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long id,
            @PathVariable Long reportId,
            Authentication auth) {
        User requester = userService.loadUserByEmail(auth.getName());
        groupService.deleteReport(id, reportId, requester);
        return ResponseEntity.ok(ApiResponse.ok("Signalement supprimé", null));
    }

    // ── Inner records ─────────────────────────────────────────────────────

    record JoinBody(String message) {}
    record RoleBody(String role) {}
}
