package com.fishdex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishdex.backend.dto.LoginRequest;
import com.fishdex.backend.dto.RegisterRequest;
import com.fishdex.backend.repository.BadgeRepository;
import com.fishdex.backend.repository.CaptureRepository;
import com.fishdex.backend.repository.GroupJoinRequestRepository;
import com.fishdex.backend.repository.GroupMemberRepository;
import com.fishdex.backend.repository.GroupRepository;
import com.fishdex.backend.repository.NotificationRepository;
import com.fishdex.backend.repository.PostRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest extends com.fishdex.backend.BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private GroupJoinRequestRepository groupJoinRequestRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private CaptureRepository captureRepository;
    @Autowired private BadgeRepository badgeRepository;

    private String token1;
    private String token2;

    @BeforeEach
    void setUp() throws Exception {
        postRepository.deleteAll();
        groupJoinRequestRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        cleanAll(); // handles notifications, token tables, captures, badges, users

        token1 = registerAndLogin("user1@fishdex.fr", "user1");
        token2 = registerAndLogin("user2@fishdex.fr", "user2");
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        groupJoinRequestRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        cleanAll();
    }

    private String registerAndLogin(String email, String username) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail(email);
        register.setUsername(username);
        register.setPassword("Motdepasse1!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("Motdepasse1!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token").asText();
    }

    /** Creates a PUBLIC group via multipart form params and returns its id */
    private Long createGroup(String name, String token) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/groups")
                        .param("name", name)
                        .param("description", "Un groupe de pêche")
                        .param("visibility", "PUBLIC")
                        .param("category", "CLUB")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();
    }

    // ── Tests ──────────────────────────────────────────────────────────────

    @Test
    void createGroup_withValidJwt_returns201() throws Exception {
        mockMvc.perform(multipart("/api/groups")
                        .param("name", "Les Brocheteurs")
                        .param("description", "Un groupe de pêche")
                        .param("category", "CLUB")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Les Brocheteurs"))
                .andExpect(jsonPath("$.data.memberCount").value(1))
                .andExpect(jsonPath("$.data.category").value("CLUB"));
    }

    @Test
    void createGroup_withoutJwt_returns401() throws Exception {
        mockMvc.perform(multipart("/api/groups")
                        .param("name", "Anonymous"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGroupById_returns200() throws Exception {
        Long groupId = createGroup("Les Carpistes", token1);

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Les Carpistes"))
                .andExpect(jsonPath("$.data.memberCount").value(1))
                .andExpect(jsonPath("$.data.myRole").value("OWNER"));
    }

    @Test
    void joinPublicGroup_returns200_andMemberCountIncreases() throws Exception {
        Long groupId = createGroup("Club Sandre", token1);

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
        Long groupId = createGroup("Club Perche", token1);

        // Owner tries to join their own group
        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isConflict());
    }

    @Test
    void leaveGroup_returns200() throws Exception {
        Long groupId = createGroup("Club Truite", token1);

        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/groups/" + groupId + "/leave")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    void discoverGroups_returns200() throws Exception {
        createGroup("Club Public", token1);

        mockMvc.perform(get("/api/groups/discover")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getMembers_returns200() throws Exception {
        Long groupId = createGroup("Club Membres", token1);

        mockMvc.perform(get("/api/groups/" + groupId + "/members")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("user1"));
    }

    @Test
    void deleteGroup_byOwner_returns200() throws Exception {
        Long groupId = createGroup("Club Éphémère", token1);

        mockMvc.perform(delete("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteGroup_byNonOwner_returns403() throws Exception {
        Long groupId = createGroup("Club Protégé", token1);

        mockMvc.perform(post("/api/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + token2));

        mockMvc.perform(delete("/api/groups/" + groupId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_inGroup_returns201() throws Exception {
        Long groupId = createGroup("Club Posts", token1);

        mockMvc.perform(post("/api/groups/" + groupId + "/posts")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"Hello groupe !\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("Hello groupe !"))
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    void getPosts_asNonMember_returns403() throws Exception {
        // Create PRIVATE group
        MvcResult result = mockMvc.perform(multipart("/api/groups")
                        .param("name", "Groupe Privé")
                        .param("visibility", "PRIVATE")
                        .param("category", "FRIENDS")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isCreated())
                .andReturn();
        Long groupId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(get("/api/groups/" + groupId + "/posts")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }
}
