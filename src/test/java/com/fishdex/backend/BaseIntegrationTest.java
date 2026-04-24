package com.fishdex.backend;

import com.fishdex.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared FK-safe cleanup helper for all integration tests.
 * Child classes keep their own @SpringBootTest / @BeforeEach / @AfterEach.
 */
public abstract class BaseIntegrationTest {

    @Autowired protected NotificationRepository notificationRepository;
    @Autowired protected EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired protected RefreshTokenRepository refreshTokenRepository;
    @Autowired protected PreAuthTokenRepository preAuthTokenRepository;
    @Autowired protected PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired protected BadgeRepository badgeRepository;
    @Autowired protected CaptureRepository captureRepository;
    @Autowired protected UserRepository userRepository;

    protected void cleanAll() {
        notificationRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        preAuthTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        captureRepository.deleteAll();
        badgeRepository.deleteAll();
        userRepository.deleteAll();
    }
}
