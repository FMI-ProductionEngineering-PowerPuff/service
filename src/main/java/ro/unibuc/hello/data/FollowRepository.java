package ro.unibuc.hello.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FollowRepository extends MongoRepository<FollowEntity, String> {
    boolean existsByUserFollowerAndUserFollowed(String userFollower, String userFollowed);
    List<FollowEntity> findByUserFollower(String userFollower);
    long countByUserFollowed(String userId);
}
