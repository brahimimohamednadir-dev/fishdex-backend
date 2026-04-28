package com.fishdex.backend.dto;

import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.Friendship;
import com.fishdex.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendResponse {

    private Long userId;
    private String username;
    private String userTag;
    private int captureCount;

    /** true si l'utilisateur a posté une capture dans les dernières 24h */
    private boolean activeToday;

    /** Aperçu de la dernière capture */
    private Long lastCaptureId;
    private String lastCaptureSpecies;
    private String lastCapturePhotoUrl;
    private LocalDateTime lastCaptureAt;

    /** Statut de l'amitié du point de vue du requêteur */
    private String friendshipStatus;  // ACCEPTED | PENDING_SENT | PENDING_RECEIVED | NONE
    private Long friendshipId;

    // ── Factory methods ───────────────────────────────────────────────────

    /** Profil complet d'un ami accepté */
    public static FriendResponse fromFriend(User other, Friendship friendship, Capture lastCapture) {
        boolean activeToday = lastCapture != null &&
                lastCapture.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24));

        return FriendResponse.builder()
                .userId(other.getId())
                .username(other.getUsername())
                .userTag(other.getUserTag())
                .captureCount(other.getCaptureCount())
                .activeToday(activeToday)
                .lastCaptureId(lastCapture != null ? lastCapture.getId() : null)
                .lastCaptureSpecies(lastCapture != null ? lastCapture.getSpeciesName() : null)
                .lastCapturePhotoUrl(lastCapture != null ? lastCapture.getPhotoUrl() : null)
                .lastCaptureAt(lastCapture != null ? lastCapture.getCaughtAt() : null)
                .friendshipStatus("ACCEPTED")
                .friendshipId(friendship.getId())
                .build();
    }

    /** Résultat de recherche utilisateur (non-ami) */
    public static FriendResponse fromSearch(User user, String status, Long friendshipId) {
        return FriendResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userTag(user.getUserTag())
                .captureCount(user.getCaptureCount())
                .activeToday(false)
                .friendshipStatus(status)
                .friendshipId(friendshipId)
                .build();
    }
}
