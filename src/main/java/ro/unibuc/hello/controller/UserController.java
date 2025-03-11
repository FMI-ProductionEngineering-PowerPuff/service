package ro.unibuc.hello.controller;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get-profile/{id}")
    public ResponseEntity<UserEntity> getUserProfile(@PathVariable String id) {
        Optional<UserEntity> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserEntity> updateUserProfile(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        Optional<UserEntity> updatedUser = userService.updateUser(id, request.getUsername(), request.getEmail(), request.getNickname(), request.getBio(), request.getAge(), request.getVegetarian());
        return updatedUser.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUserProfile(@PathVariable String id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    static class UpdateUserRequest {
        private String username;
        private String email;
        private String nickname;
        private String bio;
        private Integer age;
        private Boolean vegetarian;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Boolean getVegetarian() { return vegetarian; }
        public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }
    }
}
