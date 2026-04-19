package com.fishdex.backend.service;

import com.fishdex.backend.dto.BadgeResponse;
import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.entity.Badge.BadgeType;
import com.fishdex.backend.entity.User;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.UserRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class BadgeServiceTest {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private CaptureRepository captureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        captureRepository.deleteAll();
        badgeRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest register = new RegisterRequest();
        register.setEmail("badgetest@fishdex.fr");
        register.setUsername("badgetestuser");
        register.setPassword("motdepasse123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        testUser = userRepository.findByEmail("badgetest@fishdex.fr").orElseThrow();
    }

    @AfterEach
    void tearDown() {
        captureRepository.deleteAll();
        badgeRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createCapture(String token, String speciesName) throws Exception {
        CaptureRequest req = new CaptureRequest();
        req.setSpeciesName(speciesName);
        req.setWeight(1.0);
        req.setLength(30.0);
        req.setCaughtAt(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    private String getToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("badgetest@fishdex.fr");
        login.setPassword("motdepasse123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    @Test
    void firstCatchAwardsBadge() throws Exception {
        String token = getToken();
        createCapture(token, "Brochet");

        testUser = userRepository.findByEmail("badgetest@fishdex.fr").orElseThrow();
        List<BadgeResponse> badges = badgeService.getMyBadges("badgetest@fishdex.fr");

        assertThat(badges).extracting(BadgeResponse::getType)
                .contains(BadgeType.FIRST_CATCH.name());
    }

    @Test
    void tenCatchesAwardsBadge() throws Exception {
        String token = getToken();
        for (int i = 0; i < 10; i++) {
            createCapture(token, "Brochet");
        }

        List<BadgeResponse> badges = badgeService.getMyBadges("badgetest@fishdex.fr");

        assertThat(badges).extracting(BadgeResponse::getType)
                .contains(BadgeType.FIRST_CATCH.name(), BadgeType.TEN_CATCHES.name());
    }

    @Test
    void noDuplicateBadge() throws Exception {
        String token = getToken();
        createCapture(token, "Brochet");
        createCapture(token, "Carpe");

        long firstCatchCount = badgeRepository.findByUserIdOrderByEarnedAtDesc(testUser.getId())
                .stream()
                .filter(b -> b.getType() == BadgeType.FIRST_CATCH)
                .count();

        assertThat(firstCatchCount).isEqualTo(1);
    }

    @Test
    void speciesCollectorBadge() throws Exception {
        String token = getToken();
        createCapture(token, "Brochet");
        createCapture(token, "Carpe");
        createCapture(token, "Sandre");
        createCapture(token, "Perche");
        createCapture(token, "Truite");

        List<BadgeResponse> badges = badgeService.getMyBadges("badgetest@fishdex.fr");

        assertThat(badges).extracting(BadgeResponse::getType)
                .contains(BadgeType.SPECIES_COLLECTOR.name());
    }
}
