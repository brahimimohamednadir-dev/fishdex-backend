package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fishing_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    public enum GroupVisibility {
        PUBLIC, PRIVATE, SECRET
    }

    public enum GroupCategory {
        CLUB, ASSOCIATION, FRIENDS, COMPETITION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupVisibility visibility = GroupVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupCategory category = GroupCategory.FRIENDS;

    @Column(name = "cover_photo_url")
    private String coverPhotoUrl;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Integer postCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
