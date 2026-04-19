package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.entity.Capture;
import com.fishdex.backend.entity.User;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CaptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaptureRepository captureRepository;

    private String token;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        captureRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest register = new RegisterRequest();
        register.setEmail("pecheur@fishdex.fr");
        register.setUsername("pecheur1");
        register.setPassword("motdepasse123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("pecheur@fishdex.fr");
        login.setPassword("motdepasse123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).at("/data/token").asText();
        testUser = userRepository.findByEmail("pecheur@fishdex.fr").orElseThrow();
    }

    @AfterEach
    void tearDown() {
        captureRepository.deleteAll();
        userRepository.deleteAll();
    }

    private CaptureRequest buildCaptureRequest(String speciesName) {
        CaptureRequest req = new CaptureRequest();
        req.setSpeciesName(speciesName);
        req.setWeight(1.5);
        req.setLength(35.0);
        req.setCaughtAt(LocalDateTime.now().minusHours(1));
        return req;
    }

    @Test
    void createCapture_withValidJwt_returns201() throws Exception {
        CaptureRequest request = buildCaptureRequest("Brochet");

        mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.speciesName").value("Brochet"))
                .andExpect(jsonPath("$.data.weight").value(1.5))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void createCapture_withoutJwt_returns401() throws Exception {
        CaptureRequest request = buildCaptureRequest("Carpe");

        mockMvc.perform(post("/api/captures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCapture_invalidRequest_returns400() throws Exception {
        CaptureRequest request = new CaptureRequest();
        // speciesName manquant, weight manquant

        mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyCaptures_paginee_returns200() throws Exception {
        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCaptureRequest("Brochet"))));

        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCaptureRequest("Carpe"))));

        mockMvc.perform(get("/api/captures?page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getCaptureById_ownCapture_returns200() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCaptureRequest("Sandre"))))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(get("/api/captures/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.speciesName").value("Sandre"));
    }

    @Test
    void updateCapture_ownCapture_returns200() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCaptureRequest("Brochet"))))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        CaptureRequest update = buildCaptureRequest("Brochet modifié");
        update.setWeight(3.0);

        mockMvc.perform(put("/api/captures/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.speciesName").value("Brochet modifié"))
                .andExpect(jsonPath("$.data.weight").value(3.0));
    }

    @Test
    void deleteCapture_ownCapture_returns204() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCaptureRequest("Perche"))))
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(delete("/api/captures/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/captures/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCapture_freemiumLimit_returns403() throws Exception {
        // Insertion directe de 50 captures en base pour simuler la limite
        List<Capture> captures = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            captures.add(Capture.builder()
                    .user(testUser)
                    .speciesName("Espèce " + i)
                    .weight(1.0)
                    .length(20.0)
                    .caughtAt(LocalDateTime.now().minusDays(i))
                    .build());
        }
        captureRepository.saveAll(captures);

        mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCaptureRequest("Brochet"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("50")));
    }

    @Test
    void getCaptureById_anotherUsersCapture_returns403() throws Exception {
        // Créer un second utilisateur
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("autre@fishdex.fr");
        register2.setUsername("autreuser");
        register2.setPassword("motdepasse123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)));

        LoginRequest login2 = new LoginRequest();
        login2.setEmail("autre@fishdex.fr");
        login2.setPassword("motdepasse123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andReturn();
        String token2 = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/data/token").asText();

        // Créer une capture avec user1
        MvcResult result = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCaptureRequest("Brochet"))))
                .andReturn();
        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        // Tenter d'y accéder avec user2
        mockMvc.perform(get("/api/captures/" + id)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }
}
