package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Friendship;
import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Récupère une amitié entre deux utilisateurs (dans les deux sens) */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :u1 AND f.addressee = :u2) OR " +
           "(f.requester = :u2 AND f.addressee = :u1)")
    Optional<Friendship> findBetween(@Param("u1") User u1, @Param("u2") User u2);

    /** Liste toutes les amitiés acceptées d'un utilisateur */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    /** Liste les IDs des amis acceptés d'un utilisateur */
    @Query("SELECT CASE WHEN f.requester = :user THEN f.addressee.id ELSE f.requester.id END " +
           "FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Long> findFriendIds(@Param("user") User user);

    /** Demandes en attente reçues par cet utilisateur */
    @Query("SELECT f FROM Friendship f WHERE f.addressee = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingReceived(@Param("user") User user);

    /** Demandes en attente envoyées par cet utilisateur */
    @Query("SELECT f FROM Friendship f WHERE f.requester = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingSent(@Param("user") User user);

    /** Vérifie si deux utilisateurs sont amis */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
           "((f.requester = :u1 AND f.addressee = :u2) OR (f.requester = :u2 AND f.addressee = :u1)) " +
           "AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("u1") User u1, @Param("u2") User u2);
}
