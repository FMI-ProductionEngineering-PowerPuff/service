package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;
import ro.unibuc.hello.service.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private final String rawPassword = "Password1";
    private final String hashedPassword = "mockedHashedPassword";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository);
    }

    @Test
    void test_register_success() {
        UserEntity user = new UserEntity("user", "user@test.com", rawPassword, UserRole.USER, "nick", "bio", 25, true, false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity result = authService.register(user);

        assertEquals(authService.hashPassword(rawPassword), result.getPassword());
        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void test_register_emailAlreadyInUse() {
        UserEntity user = new UserEntity("user", "exists@test.com", rawPassword, UserRole.USER, "nick", "bio", 25, true, false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.register(user));
        assertEquals("Email already in use.", exception.getMessage());
    }

    @Test
    void test_register_invalidPassword() {
        UserEntity user = new UserEntity("user", "user@test.com", "pass", UserRole.USER, "nick", "bio", 25, true, false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.register(user));
        assertTrue(exception.getMessage().contains("Password must have"));
    }

    @Test
    void test_register_chefUnderage() {
        UserEntity user = new UserEntity("user", "user@test.com", rawPassword, UserRole.CHEF, "nick", "bio", 16, true, false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.register(user));
        assertEquals("Chef must be 18+.", exception.getMessage());
    }

    @Test
    void test_validatePassword_valid() {
        assertTrue(authService.validatePassword("ValidPass1"));
    }

    @Test
    void test_validatePassword_invalid() {
        assertFalse(authService.validatePassword("short"));
        assertFalse(authService.validatePassword("nouppercase1"));
        assertFalse(authService.validatePassword("NoNumber"));
    }

    @Test
    void test_login_success() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword(authService.hashPassword(rawPassword));
        
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<UserEntity> result = authService.login(user.getEmail(), rawPassword);
        assertTrue(result.isPresent());
    }

    @Test
    void test_login_invalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        Optional<UserEntity> result = authService.login("test@example.com", "wrongpass");
        assertTrue(result.isEmpty());
    }

    @Test
    void test_changePassword_success() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword(authService.hashPassword(rawPassword));
    
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    
        String expectedNewHashed = authService.hashPassword("newPassword1");
    
        boolean result = authService.changePassword(user.getEmail(), rawPassword, "newPassword1");
    
        assertTrue(result);
        verify(userRepository).save(argThat(savedUser ->
            savedUser.getPassword().equals(expectedNewHashed)
        ));
    }

    @Test
    void test_changePassword_invalidOldPassword() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword("differentHash");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authService.changePassword(user.getEmail(), rawPassword, "Newpass1"));
        assertEquals("Invalid old password.", exception.getMessage());
    }

    @Test
    void test_changePassword_invalidNewPassword() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword(hashedPassword);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authService.changePassword(user.getEmail(), rawPassword, "short"));

        assertTrue(exception.getMessage().contains("Password must have"));
    }

    @Test
    void test_changePassword_userNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authService.changePassword("notfound@example.com", "old", "NewPass1"));

        assertEquals("User not found.", exception.getMessage());
    }
}
