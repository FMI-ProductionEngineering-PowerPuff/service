package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;

import ro.unibuc.hello.data.ContributorRepository;
import ro.unibuc.hello.data.ContributorEntity;
import ro.unibuc.hello.data.FollowEntity;
import ro.unibuc.hello.data.FollowRepository;
import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.data.RecipeRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ContributorRepository contributorRepository;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, FollowRepository followRepository, ContributorRepository contributorRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.contributorRepository = contributorRepository;
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

        // type poate sa fie Drink sau Food
        if (!recipe.getType().equalsIgnoreCase("Drink") && !recipe.getType().equalsIgnoreCase("Food")) {
            throw new IllegalArgumentException("Invalid type. Type must be 'Drink' or 'Food'.");
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
                    .filter(recipe -> !recipe.getType().equalsIgnoreCase("Drink"))
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

        if ("Drink".equalsIgnoreCase(recipe.getType()) && user.getAge() < 18) {
            throw new IllegalArgumentException("You must be 18+ to view this drink recipe.");
        }

        if (user.getVegetarian() && !recipe.getVegetarian()) {
            throw new IllegalArgumentException("This recipe is not vegetarian.");
        }

        return Optional.of(recipe);
    }

    public List<RecipeEntity> getRecipesFromFollowedChefs(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    
        Set<String> followedChefIds = followRepository.findByUserFollower(userId)
                .stream()
                .map(FollowEntity::getUserFollowedId)
                .collect(Collectors.toSet());
    
        System.out.println("Followed Chef IDs: " + followedChefIds);
    
        if (followedChefIds.isEmpty()) {
            return List.of();
        }
    
        List<RecipeEntity> recipes = recipeRepository.findByUserIdInOrderByFavoriteCountDesc(followedChefIds);
    
        System.out.println("Retrieved Recipes: " + recipes);
    
        return recipes;
    }    

    public Optional<RecipeEntity> updateRecipe(String recipeId, String userId, RecipeEntity updatedRecipe) {
        Optional<RecipeEntity> recipeOpt = recipeRepository.findById(recipeId);

        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found.");
        }

        RecipeEntity existingRecipe = recipeOpt.get();

        // trebuie adaugata verificarea ca user-ul sa faca parte din contributors la reteta (cand o sa avem contributors)

        if (!updatedRecipe.getType().equalsIgnoreCase("Food") && !updatedRecipe.getType().equalsIgnoreCase("Drink")) {
            throw new IllegalArgumentException("Invalid type. Type must be 'Food' or 'Drink'.");
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
    
    public String addContributor(String loggedInUserId, String chefId, String recipeId) {
        RecipeEntity recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found."));
    
        if (!recipe.getUserId().equals(loggedInUserId)) {
            throw new IllegalArgumentException("You can only add contributors to recipes you own.");
        }
    
        UserEntity chef = userRepository.findById(chefId)
                .orElseThrow(() -> new IllegalArgumentException("Chef not found."));
    
        if (chef.getRole() != UserRole.CHEF) {
            throw new IllegalArgumentException("Only chefs can be contributors.");
        }
    
        if (!chef.getAuthorization()) {
            throw new IllegalArgumentException("The chef must be authorized.");
        }
    
        long recipeCount = recipeRepository.countByUserId(chefId);
        if (recipeCount < 3) {
            throw new IllegalArgumentException("The chef must have at least 3 recipes.");
        }
    
        long followerCount = followRepository.countByUserFollowed(chefId);
        if (followerCount < 2) {
            throw new IllegalArgumentException("The chef must have at least 2 followers.");
        }
    
        if (chef.getAge() < 18) {
            throw new IllegalArgumentException("The chef must be at least 18 years old.");
        }
    
        Optional<ContributorEntity> existingContributor = contributorRepository.findByUserIdAndRecipeId(chefId, recipeId);
        if (existingContributor.isPresent()) {
            throw new IllegalArgumentException("The chef is already a contributor to this recipe.");
        }
    
        ContributorEntity newContributor = new ContributorEntity(chefId, recipeId);
        contributorRepository.save(newContributor);
    
        return "Contributor added successfully.";
    }
    
    public String removeContributor(String loggedInUserId, String chefId, String recipeId) {
        RecipeEntity recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found."));
    
        if (!recipe.getUserId().equals(loggedInUserId)) {
            throw new IllegalArgumentException("You can only remove contributors from recipes you own.");
        }
    
        Optional<ContributorEntity> contributor = contributorRepository.findByUserIdAndRecipeId(chefId, recipeId);
        if (contributor.isEmpty()) {
            throw new IllegalArgumentException("This user is not a contributor for the recipe.");
        }
    
        contributorRepository.deleteByUserIdAndRecipeId(chefId, recipeId);
        return "Contributor removed successfully.";
    }    

}
