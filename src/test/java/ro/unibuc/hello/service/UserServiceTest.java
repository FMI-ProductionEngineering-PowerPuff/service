package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_getUserById_found() {
        UserEntity user = new UserEntity();
        user.setId("123");
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.getUserById("123");

        assertTrue(result.isPresent());
        assertEquals("123", result.get().getId());
    }

    @Test
    void test_getUserById_notFound() {
        when(userRepository.findById("123")).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserById("123");

        assertTrue(result.isEmpty());
    }

    @Test
    void test_updateUser_found() {
        UserEntity user = new UserEntity();
        user.setId("1");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        Optional<UserEntity> result = userService.updateUser("1", "newUsername", "new@email.com", "newNick", "newBio", true);

        assertTrue(result.isPresent());
        assertEquals("newUsername", result.get().getUsername());
        assertEquals("new@email.com", result.get().getEmail());
        assertTrue(result.get().getVegetarian());
    }

    @Test
    void test_updateUser_notFound() {
        when(userRepository.findById("404")).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.updateUser("404", "x", "x", "x", "x", false);

        assertTrue(result.isEmpty());
    }

    @Test
    void test_deleteUser_exists() {
        when(userRepository.existsById("1")).thenReturn(true);

        boolean deleted = userService.deleteUser("1");

        assertTrue(deleted);
        verify(userRepository).deleteById("1");
    }

    @Test
    void test_deleteUser_notFound() {
        when(userRepository.existsById("404")).thenReturn(false);

        boolean deleted = userService.deleteUser("404");

        assertFalse(deleted);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void test_followUser_success() {
        UserEntity follower = new UserEntity();
        follower.setId("u1");
        follower.setRole(UserRole.USER);

        UserEntity chef = new UserEntity();
        chef.setId("u2");
        chef.setRole(UserRole.CHEF);

        when(userRepository.findById("u1")).thenReturn(Optional.of(follower));
        when(userRepository.findById("u2")).thenReturn(Optional.of(chef));
        when(followRepository.existsByUserFollowerAndUserFollowed("u1", "u2")).thenReturn(false);

        String result = userService.followUser("u1", "u2");

        assertEquals("Successfully followed the chef!", result);
        verify(followRepository).save(any(FollowEntity.class));
    }

    @Test
    void test_followUser_selfFollow() {
        String result = userService.followUser("u1", "u1");

        assertEquals("You cannot follow yourself.", result);
    }

    @Test
    void test_followUser_userNotFound() {
        when(userRepository.findById("follower")).thenReturn(Optional.empty());

        String result = userService.followUser("follower", "chef");

        assertEquals("One or both users do not exist.", result);
    }

    @Test
    void test_followUser_notUserRole() {
        UserEntity chef = new UserEntity();
        chef.setId("chef");
        chef.setRole(UserRole.CHEF);

        UserEntity chef_followed = new UserEntity();
        chef_followed.setId("chef_followed");
        chef_followed.setRole(UserRole.CHEF);

        when(userRepository.findById("chef")).thenReturn(Optional.of(chef));
        when(userRepository.findById("chef_followed")).thenReturn(Optional.of(chef_followed));

        String result = userService.followUser("chef", "chef_followed");

        assertEquals("Only users can follow chefs.", result);
    }

    @Test
    void test_followUser_targetNotChef() {
        UserEntity user = new UserEntity();
        user.setId("u1");
        user.setRole(UserRole.USER);

        UserEntity anotherUser = new UserEntity();
        anotherUser.setId("u2");
        anotherUser.setRole(UserRole.USER);

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.findById("u2")).thenReturn(Optional.of(anotherUser));

        String result = userService.followUser("u1", "u2");

        assertEquals("You can only follow chefs.", result);
    }

    @Test
    void test_followUser_alreadyFollowing() {
        UserEntity user = new UserEntity();
        user.setId("u1");
        user.setRole(UserRole.USER);

        UserEntity chef = new UserEntity();
        chef.setId("u2");
        chef.setRole(UserRole.CHEF);

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.findById("u2")).thenReturn(Optional.of(chef));
        when(followRepository.existsByUserFollowerAndUserFollowed("u1", "u2")).thenReturn(true);

        String result = userService.followUser("u1", "u2");

        assertEquals("You are already following this chef.", result);
    }
}
