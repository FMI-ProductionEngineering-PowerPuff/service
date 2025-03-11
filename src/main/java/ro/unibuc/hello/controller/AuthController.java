package ro.unibuc.hello.controller;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

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
                request.getBio(),
                request.getAge(),
                request.getAuthorization(),
                request.getVegetarian()
        );
        return authService.register(user);
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody LoginRequest request) {
        Optional<UserEntity> user = authService.login(request.getEmail(), request.getPassword());
        return user.map(value -> value.getId()).orElse("Invalid credentials!");
    }

    @PutMapping("/change-password")
    @ResponseBody
    public String changePassword(@RequestBody ChangePasswordRequest request) {
        boolean success = authService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
        return success ? "Password changed successfully!" : "Invalid old password!";
    }

    static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String nickname;
        private String bio;
        private Integer age;
        private Boolean authorization;
        private Boolean vegetarian;
        private ro.unibuc.hello.data.UserRole role;
        
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
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Boolean getAuthorization() { return authorization; }
        public void setAuthorization(Boolean authorization) { this.authorization = authorization; }
        public Boolean getVegetarian() { return vegetarian; }
        public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }
        public ro.unibuc.hello.data.UserRole getRole() { return role; }
        public void setRole(ro.unibuc.hello.data.UserRole role) { this.role = role; }
    }

    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class ChangePasswordRequest {
        private String email;
        private String oldPassword;
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
