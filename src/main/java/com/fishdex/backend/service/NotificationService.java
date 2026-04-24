package com.fishdex.backend.service;

import com.fishdex.backend.dto.NotificationResponse;
import com.fishdex.backend.entity.Notification;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.NotificationRepository;
import com.fishdex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── Lecture ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String email, Pageable pageable) {
        User user = loadUser(email);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = loadUser(email);
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    // ── Marquer lues ──────────────────────────────────────────────────────

    @Transactional
    public void markAllRead(String email) {
        User user = loadUser(email);
        notificationRepository.markAllRead(user);
    }

    @Transactional
    public void markRead(Long id, String email) {
        User user = loadUser(email);
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notification introuvable", HttpStatus.NOT_FOUND));
        if (!n.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé", HttpStatus.FORBIDDEN);
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    // ── Créer (usage interne) ─────────────────────────────────────────────

    @Transactional
    public void create(User recipient, Notification.NotificationType type,
                       String actorUsername, String groupName, Long groupId, Long postId) {
        // Ne pas notifier soi-même
        if (recipient.getUsername().equals(actorUsername)) return;

        Notification notif = Notification.builder()
                .recipient(recipient)
                .type(type)
                .actorUsername(actorUsername)
                .groupName(groupName)
                .groupId(groupId)
                .postId(postId)
                .build();
        notificationRepository.save(notif);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private User loadUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.NOT_FOUND));
    }
}
