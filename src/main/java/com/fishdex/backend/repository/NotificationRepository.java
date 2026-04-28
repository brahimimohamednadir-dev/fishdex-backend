package com.fishdex.backend.repository;

import com.fishdex.backend.entity.Notification;
import com.fishdex.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndReadFalse(User recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :recipient AND n.read = false")
    void markAllRead(@Param("recipient") User recipient);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient = :user")
    void deleteAllByRecipient(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.actor = :user")
    void deleteAllByActor(@Param("user") User user);
}
