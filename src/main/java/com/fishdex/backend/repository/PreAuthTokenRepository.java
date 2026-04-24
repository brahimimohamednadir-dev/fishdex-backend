package com.fishdex.backend.repository;

import com.fishdex.backend.entity.PreAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PreAuthTokenRepository extends JpaRepository<PreAuthToken, Long> {

    Optional<PreAuthToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PreAuthToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
