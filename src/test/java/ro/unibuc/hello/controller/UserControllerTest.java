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
import ro.unibuc.hello.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void test_getUserProfile_found() throws Exception {
        UserEntity user = new UserEntity();
        user.setId("123");
        user.setUsername("testuser");

        when(userService.getUserById("123")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/get-profile/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void test_getUserProfile_notFound() throws Exception {
        when(userService.getUserById("123")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/get-profile/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_updateUserProfile_success() throws Exception {
        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest();
        request.setUsername("updated");
        request.setEmail("updated@example.com");
        request.setNickname("newNick");
        request.setBio("Updated bio");
        request.setVegetarian(true);

        UserEntity updatedUser = new UserEntity();
        updatedUser.setId("123");
        updatedUser.setUsername("updated");

        when(userService.updateUser(eq("123"), any(), any(), any(), any(), any())).thenReturn(Optional.of(updatedUser));

        mockMvc.perform(put("/api/users/update/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void test_updateUserProfile_notFound() throws Exception {
        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest();
        request.setUsername("missing");

        when(userService.updateUser(eq("404"), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/update/404")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_deleteUserProfile_success() throws Exception {
        when(userService.deleteUser("123")).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));
    }

    @Test
    void test_deleteUserProfile_notFound() throws Exception {
        when(userService.deleteUser("404")).thenReturn(false);

        mockMvc.perform(delete("/api/users/delete/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_followUser_success() throws Exception {
        when(userService.followUser("follower123", "chef456"))
                .thenReturn("Successfully followed the chef!");

        mockMvc.perform(post("/api/users/follow/chef456?followerId=follower123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully followed the chef!"));
    }

    @Test
    void test_followUser_failure() throws Exception {
        when(userService.followUser("follower123", "chef456"))
                .thenReturn("You cannot follow yourself!");

        mockMvc.perform(post("/api/users/follow/chef456?followerId=follower123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You cannot follow yourself!"));
    }
}
