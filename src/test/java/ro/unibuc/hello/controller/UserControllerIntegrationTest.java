package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class UserControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
        .withExposedPorts(27017)
        .withSharding();

    @BeforeAll
    public static void startContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userId;
    private String chefId;
    private String userFollowedId;
    
    @BeforeEach
    public void setUpTestData() {
        userRepository.deleteAll();
    
        UserEntity user = new UserEntity("user123", "user@example.com", "Valid123", UserRole.USER, "User", "Hi", 20, true, false);
        UserEntity chef = new UserEntity("chef123", "chef@example.com", "Valid123", UserRole.CHEF, "Chef", "Hello", 30, true, true);
        UserEntity userFollowed = new UserEntity("user456", "user2@example.com", "Valid123", UserRole.USER, "User2", "Second", 22, true, true);
    
        userId = userRepository.save(user).getId();
        chefId = userRepository.save(chef).getId();
        userFollowedId = userRepository.save(userFollowed).getId();
    }

    // test get user profile - success
    @Test
    public void testGetUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/get-profile/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    // test update user - success
    @Test
    public void testUpdateUserProfile() throws Exception {
        UserController.UpdateUserRequest updateRequest = new UserController.UpdateUserRequest();
        updateRequest.setUsername("UpdatedUser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setNickname("Updated");
        updateRequest.setBio("Updated bio");
        updateRequest.setVegetarian(true);

        mockMvc.perform(put("/api/users/update/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("UpdatedUser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.nickname").value("Updated"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.vegetarian").value(true));
    }

    // test delete user - success
    @Test
    public void testDeleteUserProfile() throws Exception {
        mockMvc.perform(delete("/api/users/delete/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));
    }

    // test follow user - success
    @Test
    public void testFollowUserSuccess() throws Exception {
        mockMvc.perform(post("/api/users/follow/" + chefId)
                .param("followerId", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully followed the chef!"));
    }

    // test follow user - fail (chef tries to follow user)
    @Test
    public void testFollowUserFailsOnRoleMismatch() throws Exception {
        mockMvc.perform(post("/api/users/follow/" + userId)
                .param("followerId", chefId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only users can follow chefs."));
    }

    // test follow user - fail (user tries to follow another user, not a chef)
    @Test
    public void testFollowUserFailsWhenFollowingNonChef() throws Exception {
    mockMvc.perform(post("/api/users/follow/" + userFollowedId)
            .param("followerId", userId))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("You can only follow chefs."));
    }

    // test follow user - fail (user tries to follow himself)
    @Test
    public void testFollowUserFailsWhenFollowingSelf() throws Exception {
        mockMvc.perform(post("/api/users/follow/" + userId)
            .param("followerId", userId))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("You cannot follow yourself."));
    }
}
