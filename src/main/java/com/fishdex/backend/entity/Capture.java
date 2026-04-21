package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "captures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Capture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "species_name", nullable = false, length = 100)
    private String speciesName;

    /** Lien optionnel vers le catalogue (null si espèce non répertoriée) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id")
    private Species species;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double length;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 500)
    private String note;

    @Column(name = "caught_at", nullable = false)
    private LocalDateTime caughtAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
