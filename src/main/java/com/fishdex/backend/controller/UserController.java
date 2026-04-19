package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.UpdateUsernameRequest;
import com.fishdex.backend.dto.UserResponse;
import com.fishdex.backend.dto.UserStatsResponse;
import com.fishdex.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UserResponse response = userService.getMe(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @Valid @RequestBody UpdateUsernameRequest request,
            Authentication authentication
    ) {
        UserResponse response = userService.updateUsername(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profil mis à jour", response));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getStats(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getStats(authentication.getName())));
    }
}
