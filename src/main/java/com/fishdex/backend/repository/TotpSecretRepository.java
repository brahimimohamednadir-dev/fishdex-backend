package com.fishdex.backend.repository;

import com.fishdex.backend.entity.TotpSecret;
import com.fishdex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotpSecretRepository extends JpaRepository<TotpSecret, Long> {

    Optional<TotpSecret> findByUser(User user);

    boolean existsByUserAndEnabledTrue(User user);
}
