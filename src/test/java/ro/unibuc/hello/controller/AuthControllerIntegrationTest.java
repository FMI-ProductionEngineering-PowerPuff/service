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
import ro.unibuc.hello.data.UserRole;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class AuthControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
        .withExposedPorts(27017)
        .withSharding();

    @BeforeAll
    public static void setUp() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://host.docker.internal:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String testEmail = "test@example.com";
    private final String testPassword = "Valid123";

    // test register si login - success
    @Test
    public void testRegisterAndLogin() throws Exception {
        // register user
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword(testPassword);
        registerRequest.setNickname("Testy");
        registerRequest.setBio("Bio here");
        registerRequest.setAge(20);
        registerRequest.setAuthorization(true);
        registerRequest.setVegetarian(false);
        registerRequest.setRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail));

        // login user
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail(testEmail);
        loginRequest.setPassword(testPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not("Invalid credentials!")));
    }

    // test schimbarea parolei - success
    @Test
    public void testChangePasswordSuccess() throws Exception {
        // register user
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("changepass");
        registerRequest.setEmail("changepass@example.com");
        registerRequest.setPassword("OldPass1");
        registerRequest.setNickname("TestNick");
        registerRequest.setBio("Bio here");
        registerRequest.setAge(21);
        registerRequest.setAuthorization(true);
        registerRequest.setVegetarian(true);
        registerRequest.setRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // change password
        AuthController.ChangePasswordRequest changeRequest = new AuthController.ChangePasswordRequest();
        changeRequest.setEmail("changepass@example.com");
        changeRequest.setOldPassword("OldPass1");
        changeRequest.setNewPassword("NewPass1");

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully!"));
    }

    // test login - fail (parola gresita)
    @Test
    public void testLoginFailsWithWrongPassword() throws Exception {
        // register
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("wronglogin");
        registerRequest.setEmail("wronglogin@example.com");
        registerRequest.setPassword("CorrectPass1");
        registerRequest.setNickname("WrongNick");
        registerRequest.setBio("Bio here");
        registerRequest.setAge(25);
        registerRequest.setAuthorization(false);
        registerRequest.setVegetarian(false);
        registerRequest.setRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // try login with wrong password
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail("wronglogin@example.com");
        loginRequest.setPassword("WrongPass1");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid credentials!"));
    }
}
