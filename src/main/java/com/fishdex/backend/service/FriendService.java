package com.fishdex.backend.service;

import com.fishdex.backend.dto.FriendResponse;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.Friendship;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.FriendshipRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final CaptureRepository captureRepository;
    private final NotificationService notificationService;

    // ── Recherche ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FriendResponse> search(String query, User me) {
        if (query == null || query.isBlank()) {
            throw new BusinessException("La recherche ne peut pas être vide", HttpStatus.BAD_REQUEST);
        }
        String q = query.trim();

        // ── Recherche par tag exact : "username#12345" ou "#12345" ──────────
        List<User> users;
        int hashIdx = q.lastIndexOf('#');
        if (hashIdx >= 0) {
            String tag = q.substring(hashIdx + 1).trim();
            if (tag.matches("\\d{5}")) {
                // Recherche exacte par tag
                users = userRepository.findByUserTag(tag)
                        .filter(u -> !u.getId().equals(me.getId()))
                        .map(List::of)
                        .orElse(List.of());
            } else {
                users = List.of(); // tag invalide → aucun résultat
            }
        } else {
            // Recherche par pseudo (contient)
            if (q.length() < 2) {
                throw new BusinessException("La recherche doit contenir au moins 2 caractères", HttpStatus.BAD_REQUEST);
            }
            users = userRepository.findByUsernameContainingIgnoreCaseAndIdNot(q, me.getId());
        }

        return users.stream()
                .limit(20)
                .map(u -> {
                    Optional<Friendship> f = friendshipRepository.findBetween(me, u);
                    String status = "NONE";
                    Long fId = null;
                    if (f.isPresent()) {
                        Friendship fr = f.get();
                        fId = fr.getId();
                        status = switch (fr.getStatus()) {
                            case ACCEPTED -> "ACCEPTED";
                            case PENDING -> fr.getRequester().getId().equals(me.getId())
                                    ? "PENDING_SENT" : "PENDING_RECEIVED";
                            default -> "NONE";
                        };
                    }
                    return FriendResponse.fromSearch(u, status, fId);
                })
                .toList();
    }

    // ── Liste des amis ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FriendResponse> getMyFriends(User me) {
        return friendshipRepository.findAcceptedFriendships(me).stream()
                .map(f -> {
                    User other = f.otherUser(me);
                    Capture last = captureRepository
                            .findTopByUserIdOrderByCreatedAtDesc(other.getId())
                            .orElse(null);
                    return FriendResponse.fromFriend(other, f, last);
                })
                .toList();
    }

    // ── Demandes reçues ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FriendResponse> getPendingRequests(User me) {
        return friendshipRepository.findPendingReceived(me).stream()
                .map(f -> FriendResponse.fromSearch(f.getRequester(), "PENDING_RECEIVED", f.getId()))
                .toList();
    }

    // ── Envoyer une demande ───────────────────────────────────────────────

    @Transactional
    public FriendResponse sendRequest(Long targetUserId, User me) {
        if (me.getId().equals(targetUserId)) {
            throw new BusinessException("Impossible de s'ajouter soi-même", HttpStatus.BAD_REQUEST);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));

        if (friendshipRepository.findBetween(me, target).isPresent()) {
            throw new BusinessException("Une relation existe déjà avec cet utilisateur", HttpStatus.CONFLICT);
        }

        Friendship friendship = Friendship.builder()
                .requester(me).addressee(target).build();
        Friendship saved = friendshipRepository.save(friendship);

        notificationService.createNotification(
                target, me.getUsername() + " t'a envoyé une demande d'ami 🎣",
                "FRIEND_REQUEST", me.getId());

        log.info("Demande d'ami : {} → {}", me.getEmail(), target.getEmail());
        return FriendResponse.fromSearch(target, "PENDING_SENT", saved.getId());
    }

    // ── Accepter ──────────────────────────────────────────────────────────

    @Transactional
    public FriendResponse acceptRequest(Long friendshipId, User me) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException("Demande introuvable", HttpStatus.NOT_FOUND));

        if (!friendship.getAddressee().getId().equals(me.getId())) {
            throw new BusinessException("Tu ne peux pas accepter cette demande", HttpStatus.FORBIDDEN);
        }
        if (friendship.getStatus() != Friendship.Status.PENDING) {
            throw new BusinessException("Cette demande n'est plus en attente", HttpStatus.CONFLICT);
        }

        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(friendship);

        notificationService.createNotification(
                friendship.getRequester(), me.getUsername() + " a accepté ta demande d'ami ! 🤝",
                "FRIEND_ACCEPTED", me.getId());

        Capture last = captureRepository
                .findTopByUserIdOrderByCreatedAtDesc(friendship.getRequester().getId())
                .orElse(null);
        return FriendResponse.fromFriend(friendship.getRequester(), friendship, last);
    }

    // ── Refuser ───────────────────────────────────────────────────────────

    @Transactional
    public void rejectRequest(Long friendshipId, User me) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException("Demande introuvable", HttpStatus.NOT_FOUND));

        if (!friendship.getAddressee().getId().equals(me.getId())) {
            throw new BusinessException("Tu ne peux pas refuser cette demande", HttpStatus.FORBIDDEN);
        }
        friendship.setStatus(Friendship.Status.REJECTED);
        friendshipRepository.save(friendship);
        log.info("Demande d'ami refusée par {}", me.getEmail());
    }

    // ── Supprimer un ami ──────────────────────────────────────────────────

    @Transactional
    public void removeFriend(Long friendshipId, User me) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException("Amitié introuvable", HttpStatus.NOT_FOUND));

        if (!friendship.involves(me)) {
            throw new BusinessException("Accès refusé", HttpStatus.FORBIDDEN);
        }

        friendshipRepository.delete(friendship);
        log.info("Amitié {} supprimée par {}", friendshipId, me.getEmail());
    }
}
