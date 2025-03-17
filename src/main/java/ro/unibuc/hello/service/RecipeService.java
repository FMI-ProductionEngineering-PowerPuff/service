package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.data.RecipeRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    public Optional<RecipeEntity> addRecipe(RecipeEntity recipe) {
        // User-ul trebuie sa existe
        Optional<UserEntity> userOpt = userRepository.findById(recipe.getUserId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        
        UserEntity user = userOpt.get();

        // User-ul trebuie sa fie bucatar pt a putea posta o reteta
        if (user.getRole() != UserRole.CHEF) {
            throw new IllegalArgumentException("Only chefs can add recipes.");
        }

        // Orice reteta abia postata nu e frozen
        recipe.setFrozen(false);

        // type poate sa fie Food, Alcoholic-Drink sau Non-Alcoholic-Drink
        if (!recipe.getType().equalsIgnoreCase("Alcoholic-Drink") && !recipe.getType().equalsIgnoreCase("Non-Alcoholic-Drink") && !recipe.getType().equalsIgnoreCase("Food")) {
            throw new IllegalArgumentException("Invalid type. Type must be 'Food', 'Alcoholic-Drink' or Non-Alcoholic-Drink'.");
        }

        return Optional.of(recipeRepository.save(recipe));
    }

    public Optional<RecipeEntity> changeFrozenStatus(String recipeId, String userId) {
        Optional<RecipeEntity> recipeOpt = recipeRepository.findById(recipeId);
    
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found.");
        }
    
        RecipeEntity recipe = recipeOpt.get();
    
        // Autorul retetei e singurul care poate modifica statusul frozen
        if (!recipe.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can change the frozen status.");
        }
    
        recipe.setFrozen(!recipe.getFrozen());
    
        return Optional.of(recipeRepository.save(recipe));
    }
      
    public List<RecipeEntity> getFilteredRecipes(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        List<RecipeEntity> recipes = recipeRepository.findAllByOrderByFavoriteCountDesc();

        if (user.getAge() < 18) {
            recipes = recipes.stream()
                    .filter(recipe -> !recipe.getType().equalsIgnoreCase("Alcoholic-Drink"))
                    .collect(Collectors.toList());
        }

        if (user.getVegetarian()) {
            recipes = recipes.stream()
                    .filter(RecipeEntity::getVegetarian)
                    .collect(Collectors.toList());
        }

        return recipes;
    }

    public Optional<RecipeEntity> getRecipeById(String recipeId, String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        RecipeEntity recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found."));

        if ("Alcoholic-Drink".equalsIgnoreCase(recipe.getType()) && user.getAge() < 18) {
            throw new IllegalArgumentException("You must be 18+ to view this alcoholic drink recipe.");
        }

        if (user.getVegetarian() && !recipe.getVegetarian()) {
            throw new IllegalArgumentException("This recipe is not vegetarian.");
        }

        return Optional.of(recipe);
    }

    public Optional<RecipeEntity> updateRecipe(String recipeId, String userId, RecipeEntity updatedRecipe) {
        Optional<RecipeEntity> recipeOpt = recipeRepository.findById(recipeId);

        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found.");
        }

        RecipeEntity existingRecipe = recipeOpt.get();

        // trebuie adaugata verificarea ca user-ul sa faca parte din contributors la reteta (cand o sa avem contributors) sau sa fie autorul retetei

        if (!updatedRecipe.getType().equalsIgnoreCase("Food") && !updatedRecipe.getType().equalsIgnoreCase("Drink")) {
            throw new IllegalArgumentException("Invalid type. Type must be 'Food', 'Alcoholic-Drink' or 'Non-Alcoholic-Drink'.");
        }

        existingRecipe.setName(updatedRecipe.getName());
        existingRecipe.setDescription(updatedRecipe.getDescription());
        existingRecipe.setPhoto(updatedRecipe.getPhoto());
        existingRecipe.setCategory(updatedRecipe.getCategory());
        existingRecipe.setType(updatedRecipe.getType());
        existingRecipe.setVegetarian(updatedRecipe.getVegetarian());
        existingRecipe.setFrozen(updatedRecipe.getFrozen());

        // Save the updated recipe to the database
        return Optional.of(recipeRepository.save(existingRecipe));
    }


    public void deleteRecipe(String recipeId, String userId) {
        Optional<RecipeEntity> recipeOpt = recipeRepository.findById(recipeId);
    
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found.");
        }
    
        RecipeEntity recipe = recipeOpt.get();
    
        // Doar autorul poate sterge reteta
        if (!recipe.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can delete this recipe.");
        }
    
        recipeRepository.delete(recipe);
    }

    public RecipeEntity getFeaturedRecipe(String userId) {
        // User trb sa fie un CHEF autorizat
        UserEntity chef = userRepository.findByIdAndRoleAndAuthorization(userId, UserRole.CHEF, true)
                .orElseThrow(() -> new IllegalArgumentException("User is not an authorized chef"));

        // sa aiba cel putin 3 retete postate
        long recipeCount = recipeRepository.countByUserId(userId);
        if (recipeCount < 3) {
            throw new IllegalArgumentException("Chef must have at least 3 recipes to have a featured recipe");
        }

        // sa fi primit cel putin 4 like-uri
        long totalLikes = recipeRepository.getTotalLikesByUserId(userId).orElse(0L);

        if (totalLikes < 4) {
            throw new IllegalArgumentException("Chef's recipes must have at least 4 likes in total");
        }

        return recipeRepository.findRandomRecipeByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No recipes found for this chef"));
    }
    
}