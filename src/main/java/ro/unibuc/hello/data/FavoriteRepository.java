package ro.unibuc.hello.data;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FavoriteRepository extends MongoRepository<FavoriteEntity, String> {
    boolean existsByUserIdAndRecipeId(String userId, String recipeId);
    Optional<FavoriteEntity> findByUserIdAndRecipeId(String userId, String recipeId);
    int countByRecipeId(String recipeId);
}

