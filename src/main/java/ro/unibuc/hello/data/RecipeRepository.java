package ro.unibuc.hello.data;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
public interface RecipeRepository extends MongoRepository<RecipeEntity, String> {

    List<RecipeEntity> findAllByOrderByFavoriteCountDesc();

    @Query("{ 'category': ?0 }")
    List<RecipeEntity> findByCategory(String category);

    List<RecipeEntity> findByCategoryOrderByFavoriteCountDesc(String category);

    @Query("{ 'type': ?0 }")
    List<RecipeEntity> findByType(String type);

    @Query("{ 'vegetarian': true }")
    List<RecipeEntity> findVegetarianRecipes();

    List<RecipeEntity> findByUserIdInOrderByFavoriteCountDesc(Set<String> userIds); 
    
    long countByUserId(String userId);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $group: { _id: null, totalLikes: { $sum: '$favoriteCount' } } }"
    })
    Optional<Long> getTotalLikesByUserId(String userId);    

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $sample: { size: 1 } }"
    })
    Optional<RecipeEntity> findRandomRecipeByUserId(String userId);

}
