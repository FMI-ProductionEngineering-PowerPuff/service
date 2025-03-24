package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRole;
import ro.unibuc.hello.service.AuthService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void test_register_success() throws Exception {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole(UserRole.USER);
        request.setNickname("tester");
        request.setBio("Hello world");
        request.setAge(25);
        request.setAuthorization(true);
        request.setVegetarian(false);

        UserEntity user = new UserEntity(
            request.getUsername(), request.getEmail(), request.getPassword(),
            request.getRole(), request.getNickname(), request.getBio(),
            request.getAge(), request.getAuthorization(), request.getVegetarian()
        );

        when(authService.register(any(UserEntity.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void test_login_success() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        UserEntity user = new UserEntity();
        user.setId("1234");
        user.setEmail("test@example.com");

        when(authService.login(request.getEmail(), request.getPassword()))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("1234"));
    }

    @Test
    void test_login_failure() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("wrongpass");

        when(authService.login(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid credentials!"));
    }

    @Test
    void test_changePassword_success() throws Exception {
        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest();
        request.setEmail("test@example.com");
        request.setOldPassword("oldpass");
        request.setNewPassword("newpass");

        when(authService.changePassword(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully!"));
    }

    @Test
    void test_changePassword_invalid() throws Exception {
        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest();
        request.setEmail("test@example.com");
        request.setOldPassword("wrongOld");
        request.setNewPassword("newpass");

        when(authService.changePassword(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Old password incorrect"));

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Old password incorrect"));
    }
}
