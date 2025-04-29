package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;

import ro.unibuc.hello.data.FavoriteRepository;
import ro.unibuc.hello.data.FavoriteEntity;
import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.data.RecipeRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, RecipeRepository recipeRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    public int countFavoritesForRecipe(String recipeId) {
        return favoriteRepository.countByRecipeId(recipeId);
    }    

    public void addFavorite(String userId, String recipeId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RecipeEntity recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        if (user.getRole() == UserRole.CHEF) {
            throw new IllegalArgumentException("Chefs cannot add recipes to favorites");
        }

        if (favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            throw new IllegalArgumentException("Recipe is already in favorites");
        }

        // se adauga la favorite
        FavoriteEntity favorite = new FavoriteEntity(userId, recipeId);
        favoriteRepository.save(favorite);

        // se incrementeaza favoriteCount cu 1
        recipe.setFavoriteCount(recipe.getFavoriteCount() + 1);
        recipeRepository.save(recipe);
    }

    public void removeFavorite(String userId, String recipeId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RecipeEntity recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        if (user.getRole() == UserRole.CHEF) {
            throw new IllegalArgumentException("Chefs cannot remove recipes from favorites");
        }

        FavoriteEntity favorite = favoriteRepository.findByUserIdAndRecipeId(userId, recipeId)
                .orElseThrow(() -> new IllegalArgumentException("This recipe is not in favorites"));

        // se scoate de la favorite
        favoriteRepository.delete(favorite);

        // se decrementeaza favoriteCount cu 1
        if (recipe.getFavoriteCount() > 0) {
            recipe.setFavoriteCount(recipe.getFavoriteCount() - 1);
            recipeRepository.save(recipe);
        }
    }
}
