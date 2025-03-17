package ro.unibuc.hello.data;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ContributorRepository extends MongoRepository<ContributorEntity, String> {
    Optional<ContributorEntity> findByUserIdAndRecipeId(String userId, String recipeId);
    List<ContributorEntity> findByRecipeId(String recipeId);
    void deleteByUserIdAndRecipeId(String userId, String recipeId);
}
