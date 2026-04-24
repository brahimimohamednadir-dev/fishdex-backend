package com.fishdex.backend.service;

import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.dto.UserStatsResponse;
import com.fishdex.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class StatsServiceTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private CaptureRepository captureRepository;
    @Autowired private BadgeRepository badgeRepository;
    @Autowired private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PreAuthTokenRepository preAuthTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        cleanAll();
        RegisterRequest register = new RegisterRequest();
        register.setEmail("stats@fishdex.fr");
        register.setUsername("statsuser");
        register.setPassword("Motdepasse1!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));
    }

    @AfterEach
    void tearDown() { cleanAll(); }

    private String getToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("stats@fishdex.fr");
        login.setPassword("Motdepasse1!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    @Test
    void statsWithNoCaptures_returnsZeros() {
        UserStatsResponse stats = userService.getMyStats("stats@fishdex.fr");

        assertThat(stats.getTotalCaptures()).isEqualTo(0);
        assertThat(stats.getBiggestCatch()).isNull();
        assertThat(stats.getTotalWeight()).isEqualTo(0.0);
        assertThat(stats.getCapturesBySpecies()).isEmpty();
        assertThat(stats.getMostActiveMonth()).isNull();
        assertThat(stats.getJoinedGroupsCount()).isEqualTo(0);
    }

    @Test
    void statsWithCaptures_correctTotalWeightAndBiggestCatch() throws Exception {
        String token = getToken();

        CaptureRequest req1 = new CaptureRequest();
        req1.setSpeciesName("Brochet");
        req1.setWeight(3.5);
        req1.setLength(70.0);
        req1.setCaughtAt(LocalDateTime.now().minusHours(2));

        CaptureRequest req2 = new CaptureRequest();
        req2.setSpeciesName("Carpe");
        req2.setWeight(8.0);
        req2.setLength(90.0);
        req2.setCaughtAt(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));
        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        UserStatsResponse stats = userService.getMyStats("stats@fishdex.fr");

        assertThat(stats.getTotalCaptures()).isEqualTo(2);
        assertThat(stats.getTotalWeight()).isEqualTo(11.5);
        assertThat(stats.getBiggestCatch()).isNotNull();
        assertThat(stats.getBiggestCatch().getSpeciesName()).isEqualTo("Carpe");
        assertThat(stats.getCapturesBySpecies()).containsEntry("Brochet", 1L).containsEntry("Carpe", 1L);
        assertThat(stats.getMostActiveMonth()).isNotNull();
    }
}
