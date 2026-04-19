package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.CaptureRequest;
import com.fishdex.backend.dto.GroupRequest;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.entity.Group.GroupType;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.GroupRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private CaptureRepository captureRepository;

    private String token1;
    private String token2;

    @BeforeEach
    void setUp() throws Exception {
        captureRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        token1 = registerAndLogin("user1@fishdex.fr", "user1");
        token2 = registerAndLogin("user2@fishdex.fr", "user2");
    }

    @AfterEach
    void tearDown() {
        captureRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndLogin(String email, String username) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail(email);
        register.setUsername(username);
        register.setPassword("motdepasse123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("motdepasse123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    private GroupRequest buildGroupRequest(String name) {
        GroupRequest req = new GroupRequest();
        req.setName(name);
        req.setDescription("Un groupe de pêche");
        req.setType(GroupType.CLUB);
        return req;
    }

    @Test
    void createGroup_withValidJwt_returns201() throws Exception {
        mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Les Brocheteurs"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Les Brocheteurs"))
                .andExpect(jsonPath("$.data.memberCount").value(1))
                .andExpect(jsonPath("$.data.type").value("CLUB"));
    }

    @Test
    void createGroup_duplicateName_returns409() throws Exception {
        mockMvc.perform(post("/api/groups")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGroupRequest("Club Unique"))));

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Club Unique"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createGroup_withoutJwt_returns401() throws Exception {
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Anonymous"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroup_returns200() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Les Carpistes"))))
                .andReturn();
        Long groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Les Carpistes"))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    void joinGroup_returns200() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Club Sandre"))))
                .andReturn();
        Long groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(jsonPath("$.data.memberCount").value(2));
    }

    @Test
    void joinGroup_alreadyMember_returns409() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Club Perche"))))
                .andReturn();
        Long groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isConflict());
    }

    @Test
    void getFeed_asMember_returns200() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Club Truite"))))
                .andReturn();
        Long groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                .header("Authorization", "Bearer " + token2));

        CaptureRequest capture = new CaptureRequest();
        capture.setSpeciesName("Brochet");
        capture.setWeight(2.0);
        capture.setLength(60.0);
        capture.setCaughtAt(LocalDateTime.now().minusHours(1));
        mockMvc.perform(post("/api/captures")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(capture)));

        mockMvc.perform(get("/api/groups/" + groupId + "/feed")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].speciesName").value("Brochet"));
    }

    @Test
    void getFeed_asNonMember_returns403() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGroupRequest("Groupe Privé"))))
                .andReturn();
        Long groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(get("/api/groups/" + groupId + "/feed")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
