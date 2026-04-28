package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Celui qui envoie la demande */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** Celui qui reçoit la demande */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status { PENDING, ACCEPTED, REJECTED, BLOCKED }

    /** Retourne vrai si cet utilisateur fait partie de l'amitié */
    public boolean involves(User user) {
        return requester.getId().equals(user.getId()) || addressee.getId().equals(user.getId());
    }

    /** Retourne l'autre utilisateur */
    public User otherUser(User me) {
        return requester.getId().equals(me.getId()) ? addressee : requester;
    }
}
