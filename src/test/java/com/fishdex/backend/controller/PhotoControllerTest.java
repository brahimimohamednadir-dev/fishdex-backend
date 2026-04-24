package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.exception.BusinessException;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.*;
import com.fishdex.backend.service.CloudinaryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PhotoControllerTest extends com.fishdex.backend.BaseIntegrationTest {

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

    @MockBean
    private CloudinaryService cloudinaryService;

    private String token;
    private Long captureId;

    @BeforeEach
    void setUp() throws Exception {
        cleanAll();

        RegisterRequest register = new RegisterRequest();
        register.setEmail("photoman@fishdex.fr");
        register.setUsername("photoman");
        register.setPassword("Motdepasse1!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("photoman@fishdex.fr");
        login.setPassword("Motdepasse1!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/data/token").asText();

        // Créer une capture pour les tests de photo
        String captureJson = """
                {
                    "speciesName": "Brochet",
                    "weight": 2.5,
                    "length": 60.0,
                    "caughtAt": "2026-04-10T10:00:00"
                }
                """;
        MvcResult captureResult = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(captureJson))
                .andReturn();
        captureId = objectMapper.readTree(captureResult.getResponse().getContentAsString())
                .at("/data/id").asLong();
    }

    @AfterEach
    void tearDown() {
        cleanAll();
    }

    @Test
    void addPhoto_returns200() throws Exception {
        when(cloudinaryService.uploadPhoto(any()))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/fishdex/captures/test.jpg");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "brochet.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/captures/" + captureId + "/photo")
                        .file(photo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.photoUrl").value("https://res.cloudinary.com/demo/image/upload/fishdex/captures/test.jpg"));
    }

    @Test
    void addPhoto_invalidType_returns400() throws Exception {
        when(cloudinaryService.uploadPhoto(any()))
                .thenThrow(new BusinessException(
                        "Type de fichier non supporté. Formats acceptés : JPEG, PNG, WEBP",
                        HttpStatus.BAD_REQUEST
                ));

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "virus.exe",
                "application/octet-stream",
                "bad-content".getBytes()
        );

        mockMvc.perform(multipart("/api/captures/" + captureId + "/photo")
                        .file(photo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
