package ro.unibuc.hello.service;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> updateUser(String id, String username, String email, String nickname, String bio) {
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setNickname(nickname);
            user.setBio(bio);
            userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
