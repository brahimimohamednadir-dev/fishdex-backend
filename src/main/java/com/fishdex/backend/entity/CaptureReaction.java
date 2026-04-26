package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "capture_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"capture_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capture_id", nullable = false)
    private Capture capture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Type type = Type.LIKE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Type { LIKE, NICE_CATCH, RECORD, IMPRESSIVE }
}
