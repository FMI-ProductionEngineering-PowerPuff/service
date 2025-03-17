package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "recipes")
public class RecipeEntity {

    @Id
    private String id;

    private String userId;

    private String name;
    private String description;
    private String photo;
    private String category;
    private String type;
    private Boolean vegetarian;
    private Boolean frozen;
    private Integer favoriteCount;
    private Instant createdAt;

    public RecipeEntity() {
        this.createdAt = Instant.now();
    }

    public RecipeEntity(String userId, String name, String description, String photo, String category, String type, Boolean vegetarian, Boolean frozen, Integer favoriteCount) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.category = category;
        this.type = type;
        this.vegetarian = vegetarian;
        this.frozen = frozen;
        this.favoriteCount = favoriteCount;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(Boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}