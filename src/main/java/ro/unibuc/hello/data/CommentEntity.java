package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comments")
public class CommentEntity {

    @Id
    private String id;

    private String userId;
    private String recipeId;
    
    private String content;
    private Integer likeCount;
    private Integer reportCount;
    private Boolean toBeReviewed;
    private Instant createdAt;

    public CommentEntity() {
        this.createdAt = Instant.now();
    }

    public CommentEntity(String userId, String recipeId, String content, Integer likeCount, Integer reportCount, Boolean toBeReviewed) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.content = content;
        this.likeCount = likeCount;
        this.reportCount = reportCount;
        this.toBeReviewed = toBeReviewed;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Boolean getToBeReviewed() {
        return toBeReviewed;
    }

    public void setToBeReviewed(Boolean toBeReviewed) {
        this.toBeReviewed = toBeReviewed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}