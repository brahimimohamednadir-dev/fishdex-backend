package com.fishdex.backend.repository;

import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByGoogleId(String googleId);

    /** Recherche par pseudo (pour la recherche d'amis) */
    List<User> findByUsernameContainingIgnoreCaseAndIdNot(String username, Long excludeId);

    /** Recherche exacte par tag 5 chiffres */
    Optional<User> findByUserTag(String userTag);

    /** Vérifie si un tag est déjà pris */
    boolean existsByUserTag(String userTag);
}
