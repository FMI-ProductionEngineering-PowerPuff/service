package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "follows")
public class FollowEntity {

    @Id
    private String id;

    private String userFollower;
    private String userFollowed;

    public FollowEntity(String userFollower, String userFollowed) {
        this.userFollower = userFollower;
        this.userFollowed = userFollowed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserFollowerId() {
        return userFollower;
    }

    public void setUserFollowerId(String userFollower) {
        this.userFollower = userFollower;
    }

    public String getUserFollowedId() {
        return userFollowed;
    }

    public void setUserFollowedId(String userFollowed) {
        this.userFollowed = userFollowed;
    }
}