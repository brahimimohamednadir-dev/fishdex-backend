package com.fishdex.backend.service;

import com.fishdex.backend.entity.PreAuthToken;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.PreAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreAuthTokenService {

    private final PreAuthTokenRepository preAuthTokenRepository;

    /** Crée un token intermédiaire valable 5 minutes pour le flux 2FA */
    @Transactional
    public PreAuthToken create(User user) {
        preAuthTokenRepository.deleteByUserId(user.getId());

        PreAuthToken token = PreAuthToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        return preAuthTokenRepository.save(token);
    }

    @Transactional
    public PreAuthToken validateAndConsume(String tokenValue) {
        PreAuthToken token = preAuthTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Token d'authentification invalide", HttpStatus.UNAUTHORIZED));

        if (!token.isValid()) {
            throw new BusinessException("Token d'authentification expiré ou déjà utilisé", HttpStatus.UNAUTHORIZED);
        }

        token.setUsed(true);
        return preAuthTokenRepository.save(token);
    }
}
