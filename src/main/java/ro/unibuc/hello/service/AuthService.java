package ro.unibuc.hello.service;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity register(UserEntity user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use.");
        }
    
        if (!validatePassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must have at least one uppercase letter, one number, and a minimum length of 8 characters.");
        }
    
        if (user.getAge() < 18 && user.getRole() == UserRole.CHEF) {
            throw new IllegalArgumentException("Chef must be 18+.");
        }
    
        user.setPassword(hashPassword(user.getPassword()));
    
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
    
        return userRepository.save(user);
    }    

    public boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$");
    }

    public Optional<UserEntity> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(hashPassword(password)));
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
    
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
    
        UserEntity user = userOpt.get();
    
        if (!validatePassword(newPassword)) {
            throw new IllegalArgumentException("Password must have at least one uppercase letter, one number, and a minimum length of 8 characters.");
        }
    
        if (!user.getPassword().equals(hashPassword(oldPassword))) {
            throw new IllegalArgumentException("Invalid old password.");
        }
    
        user.setPassword(hashPassword(newPassword));
        userRepository.save(user);
        return true;
    }    

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
