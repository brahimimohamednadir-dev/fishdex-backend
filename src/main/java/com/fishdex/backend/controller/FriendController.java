package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.FriendResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.service.FriendService;
import com.fishdex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    /** GET /api/friends — mes amis */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getMyFriends(Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getMyFriends(me)));
    }

    /** GET /api/friends/search?q=marc — recherche */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> search(
            @RequestParam String q, Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(friendService.search(q, me)));
    }

    /** GET /api/friends/requests — demandes reçues */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getPendingRequests(Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(friendService.getPendingRequests(me)));
    }

    /** POST /api/friends/request/{userId} — envoyer une demande */
    @PostMapping("/request/{userId}")
    public ResponseEntity<ApiResponse<FriendResponse>> sendRequest(
            @PathVariable Long userId, Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Demande envoyée", friendService.sendRequest(userId, me)));
    }

    /** POST /api/friends/{friendshipId}/accept — accepter */
    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<FriendResponse>> accept(
            @PathVariable Long friendshipId, Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Demande acceptée !", friendService.acceptRequest(friendshipId, me)));
    }

    /** DELETE /api/friends/{friendshipId}/reject — refuser */
    @DeleteMapping("/{friendshipId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long friendshipId, Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        friendService.rejectRequest(friendshipId, me);
        return ResponseEntity.ok(ApiResponse.ok("Demande refusée", null));
    }

    /** DELETE /api/friends/{friendshipId} — supprimer un ami */
    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable Long friendshipId, Authentication auth) {
        User me = userService.loadUserByEmail(auth.getName());
        friendService.removeFriend(friendshipId, me);
        return ResponseEntity.ok(ApiResponse.ok("Ami supprimé", null));
    }
}
