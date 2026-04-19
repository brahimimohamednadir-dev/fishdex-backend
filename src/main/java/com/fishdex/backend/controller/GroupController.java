package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.dto.FeedItemResponse;
import com.fishdex.backend.dto.GroupRequest;
import com.fishdex.backend.dto.GroupResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.service.GroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody GroupRequest request,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        GroupResponse response = groupService.createGroup(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Groupe créé", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroup(id)));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> joinGroup(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        groupService.joinGroup(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Vous avez rejoint le groupe", null));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<ApiResponse<Page<FeedItemResponse>>> getFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        User user = userService.loadUserByEmail(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<FeedItemResponse> feed = groupService.getFeed(id, user, pageable);
        return ResponseEntity.ok(ApiResponse.ok(feed));
    }
}
