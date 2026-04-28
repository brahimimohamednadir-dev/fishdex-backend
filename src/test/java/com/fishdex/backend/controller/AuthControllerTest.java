package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.ForgotPasswordRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RefreshTokenRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.repository.EmailVerificationTokenRepository;
import com.fishdex.backend.repository.PasswordResetTokenRepository;
import com.fishdex.backend.repository.PreAuthTokenRepository;
import com.fishdex.backend.repository.RefreshTokenRepository;
import com.fishdex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private PreAuthTokenRepository preAuthTokenRepository;

    /** Mot de passe valide selon @ValidPassword : 8+ chars, 1 majuscule, 1 chiffre, 1 spécial */
    private static final String VALID_PASSWORD = "Motdepasse1!";

    @Autowired
    private BadgeRepository badgeRepository;

    @BeforeEach
    void setUp() {
        preAuthTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── Register ──────────────────────────────────────────────────────────

    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@fishdex.fr");
        request.setUsername("pescateur1");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@fishdex.fr"))
                .andExpect(jsonPath("$.data.username").value("pescateur1"))
                .andExpect(jsonPath("$.data.isPremium").value(false))
                .andExpect(jsonPath("$.data.captureCount").value(0))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("doublon@fishdex.fr");
        request.setUsername("user1");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        request.setUsername("user2");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("pas-un-email");
        request.setUsername("user1");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@fishdex.fr");
        request.setUsername("user1");
        request.setPassword("court"); // pas de majuscule, chiffre, ni spécial

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsTokensAndUser() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("login@fishdex.fr");
        register.setUsername("loginuser");
        register.setPassword(VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("login@fishdex.fr");
        login.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@fishdex.fr"))
                .andExpect(jsonPath("$.data.requiresTwoFactor").value(false));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("wrongpwd@fishdex.fr");
        register.setUsername("wrongpwduser");
        register.setPassword(VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("wrongpwd@fishdex.fr");
        login.setPassword("mauvaismdp");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("inconnu@fishdex.fr");
        login.setPassword(VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // ── Refresh token ─────────────────────────────────────────────────────

    @Test
    void refresh_withValidToken_returnsNewAccessToken() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("refresh@fishdex.fr");
        register.setUsername("refreshuser");
        register.setPassword(VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("refresh@fishdex.fr");
        login.setPassword(VALID_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        String refreshToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .at("/data/refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber());
    }

    @Test
    void refresh_withInvalidToken_returns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token-completement-invalide");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @Test
    void logout_revokesRefreshToken() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("logout@fishdex.fr");
        register.setUsername("logoutuser");
        register.setPassword(VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("logout@fishdex.fr");
        login.setPassword(VALID_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        String accessToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .at("/data/token").asText();
        String refreshToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .at("/data/refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ── Forgot password (anti-énumération) ───────────────────────────────

    @Test
    void forgotPassword_alwaysReturns200() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("inexistant@fishdex.fr");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
