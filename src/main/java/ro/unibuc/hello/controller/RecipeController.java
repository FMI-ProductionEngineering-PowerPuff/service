package ro.unibuc.hello.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.Tag;
import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.service.FavoriteService;
import ro.unibuc.hello.service.RecipeService;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final FavoriteService favoriteService;
    private final MeterRegistry meterRegistry;

    public RecipeController(RecipeService recipeService, FavoriteService favoriteService, MeterRegistry meterRegistry) {
        this.recipeService = recipeService;
        this.favoriteService = favoriteService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRecipe(@RequestBody RecipeEntity recipe) {
        try {
            Optional<RecipeEntity> newRecipe = recipeService.addRecipe(recipe);
            return ResponseEntity.ok(newRecipe.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-status/{recipeId}")
    public ResponseEntity<?> changeFrozenStatus(@PathVariable String recipeId, @RequestParam("userId") String userId) {
        try {
            Optional<RecipeEntity> updatedRecipe = recipeService.changeFrozenStatus(recipeId, userId);
            return ResponseEntity.ok(updatedRecipe.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-recipes/{userId}")
    public ResponseEntity<List<RecipeEntity>> getRecipes(@PathVariable String userId) {
        List<RecipeEntity> recipes = recipeService.getFilteredRecipes(userId);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/get-recipe/{recipeId}")
    public ResponseEntity<?> getRecipeById(@PathVariable String recipeId, @RequestParam("userId") String userId) {
        try {
            Optional<RecipeEntity> recipe = recipeService.getRecipeById(recipeId, userId);
            return ResponseEntity.ok(recipe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-recipes-by-followed-chefs")
    public ResponseEntity<List<RecipeEntity>> getRecipesFromFollowedChefs(@RequestParam String userId) {
        List<RecipeEntity> recipes = recipeService.getRecipesFromFollowedChefs(userId);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/category")
    public ResponseEntity<?> getRecipesByCategory(@RequestParam String category) {
        try {
            List<RecipeEntity> recipes = recipeService.getRecipesByCategorySortedByPopularity(category);
            meterRegistry.counter("recipes.byCategory.requested.count").increment();
            return ResponseEntity.ok(recipes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{recipeId}")
    public ResponseEntity<?> updateRecipe(@PathVariable String recipeId, 
                                        @RequestParam("userId") String userId, 
                                        @RequestBody RecipeEntity updatedRecipe) {
        try {
            Optional<RecipeEntity> recipe = recipeService.updateRecipe(recipeId, userId, updatedRecipe);
            if (recipe.isPresent()) {
                return ResponseEntity.ok(recipe.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update recipe.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{recipeId}")
    public ResponseEntity<String> deleteRecipe(@PathVariable String recipeId, @RequestParam String userId) {
        try {
            recipeService.deleteRecipe(recipeId, userId);
            meterRegistry.counter("recipes.deleted.count").increment();
            return ResponseEntity.ok("Recipe deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the recipe.");
        }
    }

    @PostMapping("/add-contributor")
    public ResponseEntity<String> addContributor(@RequestParam String loggedInUserId, @RequestParam String chefId, @RequestParam String recipeId) {
        try {
            String result = recipeService.addContributor(loggedInUserId, chefId, recipeId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove-contributor")
    public ResponseEntity<String> removeContributor(@RequestParam String loggedInUserId, @RequestParam String chefId, @RequestParam String recipeId) {
        try {
            String result = recipeService.removeContributor(loggedInUserId, chefId, recipeId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/featured/{userId}")
    public ResponseEntity<?> getFeaturedRecipe(@PathVariable String userId) {
        try {
            RecipeEntity featuredRecipe = recipeService.getFeaturedRecipe(userId);
            return ResponseEntity.ok(featuredRecipe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add-to-favorites/{recipeId}")
    public ResponseEntity<String> addFavorite(@PathVariable String recipeId, @RequestParam String userId) {
        try {
            favoriteService.addFavorite(userId, recipeId);
            meterRegistry.counter("favorites.added.count").increment();
    
            meterRegistry.gauge("favorites.per.recipe.count",
                    List.of(Tag.of("recipeId", recipeId)),
                    favoriteService,
                    svc -> svc.countFavoritesForRecipe(recipeId)
            );
    
            return ResponseEntity.ok("Recipe added to favorites successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/remove-from-favorites/{recipeId}")
    public ResponseEntity<String> removeFavorite(@PathVariable String recipeId, @RequestParam String userId) {
        try {
            favoriteService.removeFavorite(userId, recipeId);
    
            meterRegistry.gauge("favorites.per.recipe.count",
                    List.of(Tag.of("recipeId", recipeId)),
                    favoriteService,
                    svc -> svc.countFavoritesForRecipe(recipeId)
            );
    
            return ResponseEntity.ok("Recipe removed from favorites successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    
}