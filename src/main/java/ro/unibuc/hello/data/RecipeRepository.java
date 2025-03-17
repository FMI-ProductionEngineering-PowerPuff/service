package ro.unibuc.hello.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends MongoRepository<RecipeEntity, String> {

    List<RecipeEntity> findAllByOrderByFavoriteCountDesc();

    @Query("{ 'type': ?0 }")
    List<RecipeEntity> findByType(String type);

    @Query("{ 'vegetarian': true }")
    List<RecipeEntity> findVegetarianRecipes();
}
