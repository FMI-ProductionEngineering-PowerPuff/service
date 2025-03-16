package ro.unibuc.hello.service;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.FollowEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;
import ro.unibuc.hello.data.FollowRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public Optional<UserEntity> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> updateUser(String id, String username, String email, String nickname, String bio, Integer age, Boolean vegetarian) {
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setNickname(nickname);
            user.setBio(bio);
            user.setAge(age);
            user.setVegetarian(vegetarian);
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

    public String followUser(String followerId, String userId) {
        if (followerId.equals(userId)) {
            return "You cannot follow yourself.";
        }

        Optional<UserEntity> followerOpt = userRepository.findById(followerId);
        Optional<UserEntity> userToFollowOpt = userRepository.findById(userId);

        if (followerOpt.isEmpty() || userToFollowOpt.isEmpty()) {
            return "One or both users do not exist.";
        }

        UserEntity follower = followerOpt.get();
        UserEntity userToFollow = userToFollowOpt.get();

        if (follower.getRole() != UserRole.USER) {
            return "Only users can follow chefs.";
        }

        if (userToFollow.getRole() != UserRole.CHEF) {
            return "You can only follow chefs.";
        }

        boolean alreadyFollowing = followRepository.existsByUserFollowerAndUserFollowed(followerId, userId);
        if (alreadyFollowing) {
            return "You are already following this chef.";
        }

        FollowEntity follow = new FollowEntity(followerId, userId);
        followRepository.save(follow);
        return "Successfully followed the chef!";
    }

}
