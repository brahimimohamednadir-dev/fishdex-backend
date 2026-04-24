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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SpeciesControllerTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        cleanAll();

        RegisterRequest register = new RegisterRequest();
        register.setEmail("pecheur@fishdex.fr");
        register.setUsername("pecheur1");
        register.setPassword("Motdepasse1!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("pecheur@fishdex.fr");
        login.setPassword("Motdepasse1!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        token = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    @Test
    void getSpecies_returns200WithSeededData() throws Exception {
        mockMvc.perform(get("/api/species")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(20));
    }

    @Test
    void getSpecies_withSearch_returnsFiltered() throws Exception {
        mockMvc.perform(get("/api/species?search=brochet")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].commonName").value("Brochet"));
    }

    @Test
    void getSpeciesById_returns200() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/species")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        Long firstId = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .at("/data/content/0/id").asLong();

        mockMvc.perform(get("/api/species/" + firstId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(firstId))
                .andExpect(jsonPath("$.data.latinName").isNotEmpty());
    }

    @Test
    void getSpeciesById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/species/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getSpecies_withoutJwt_returns200() throws Exception {
        // /api/species est public — catalogue accessible sans connexion
        mockMvc.perform(get("/api/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(20));
    }
}
