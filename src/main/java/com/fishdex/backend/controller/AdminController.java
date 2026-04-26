package com.fishdex.backend.controller;

import com.fishdex.backend.common.ApiResponse;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoint temporaire d'administration — à supprimer après usage.
 * GET /api/admin/users — liste tous les comptes avec leurs tags
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SecureRandom rng = new SecureRandom();

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listUsers(
            @RequestParam(defaultValue = "") String secret) {

        if (!"fishdex-admin-2026".equals(secret)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Accès refusé"));
        }

        // Attribuer les tags manquants au passage
        List<User> all = userRepository.findAll();
        boolean updated = false;
        for (User u : all) {
            if (u.getUserTag() == null || u.getUserTag().isBlank()) {
                u.setUserTag(generateUniqueTag());
                userRepository.save(u);
                updated = true;
            }
        }
        if (updated) all = userRepository.findAll(); // reload

        List<Map<String, Object>> result = all.stream()
                .map(u -> Map.<String, Object>of(
                        "id",       u.getId(),
                        "username", u.getUsername(),
                        "email",    u.getEmail(),
                        "userTag",  u.getUserTag() != null ? u.getUserTag() : "—",
                        "tag",      u.getUsername() + "#" + u.getUserTag()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String generateUniqueTag() {
        for (int i = 0; i < 200; i++) {
            String tag = String.format("%05d", rng.nextInt(100_000));
            if (!userRepository.existsByUserTag(tag)) return tag;
        }
        throw new RuntimeException("Impossible de générer un tag unique");
    }
}
