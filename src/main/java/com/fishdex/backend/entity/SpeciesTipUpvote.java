package com.fishdex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Table de liaison pour les upvotes de tips — évite les doublons.
 */
@Entity
@Table(name = "species_tip_upvotes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tip_id", "user_id"},
                                             name = "uk_tip_upvote"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesTipUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tip_id", nullable = false)
    private SpeciesTip tip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
