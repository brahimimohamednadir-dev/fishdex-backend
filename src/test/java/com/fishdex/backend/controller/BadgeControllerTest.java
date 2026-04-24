package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BadgeControllerTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaptureRepository captureRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        cleanAll();

        RegisterRequest register = new RegisterRequest();
        register.setEmail("badgeuser@fishdex.fr");
        register.setUsername("badgeuser");
        register.setPassword("Motdepasse1!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("badgeuser@fishdex.fr");
        login.setPassword("Motdepasse1!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        token = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    @AfterEach
    void tearDown() {
        cleanAll();
    }

    private CaptureRequest buildCapture(String speciesName) {
        CaptureRequest req = new CaptureRequest();
        req.setSpeciesName(speciesName);
        req.setWeight(1.5);
        req.setLength(30.0);
        req.setCaughtAt(LocalDateTime.now().minusHours(1));
        return req;
    }

    @Test
    void getMyBadges_noCaptures_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/badges/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getMyBadges_afterFirstCapture_returnsFirstCatchBadge() throws Exception {
        // Créer une capture → déclenche FIRST_CATCH badge
        mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCapture("Brochet"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/badges/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.type == 'FIRST_CAPTURE')]").exists())
                .andExpect(jsonPath("$.data[?(@.label == 'Première prise')]").exists());
    }
}
