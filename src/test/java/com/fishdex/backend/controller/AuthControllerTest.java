package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @BeforeEach
    void setUp() {
        cleanAll();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanAll();
    }

    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@fishdex.fr");
        request.setUsername("pescateur1");
        request.setPassword("Motdepasse1!");

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
        request.setPassword("Motdepasse1!");

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
        request.setPassword("Motdepasse1!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@fishdex.fr");
        request.setUsername("user1");
        request.setPassword("court");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("login@fishdex.fr");
        register.setUsername("loginuser");
        register.setPassword("Motdepasse1!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("login@fishdex.fr");
        login.setPassword("Motdepasse1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("login@fishdex.fr"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("wrongpwd@fishdex.fr");
        register.setUsername("wrongpwduser");
        register.setPassword("Motdepasse1!");

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
        login.setPassword("Motdepasse1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
