package main.java.ro.unibuc.hello.controller;

import main.java.ro.unibuc.hello.data.UserEntity;
import main.java.ro.unibuc.hello.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseBody
    public UserEntity register(@RequestBody RegisterRequest request) {
        UserEntity user = new UserEntity(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getNickname(),
                request.getBio()
        );
        return authService.register(user);
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody LoginRequest request) {
        Optional<UserEntity> user = authService.login(request.getEmail(), request.getPassword());
        return user.isPresent() ? "Login successful!" : "Invalid credentials!";
    }

    static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String nickname;
        private String bio;
        private main.java.ro.unibuc.hello.data.UserRole role;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public main.java.ro.unibuc.hello.data.UserRole getRole() { return role; }
        public void setRole(main.java.ro.unibuc.hello.data.UserRole role) { this.role = role; }
    }

    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
