package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FavoriteServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    


    @Test
    void test_addFavorite_success() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setFavoriteCount(0);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));
        when(favoriteRepository.existsByUserIdAndRecipeId("user123", "recipe123")).thenReturn(false);

        favoriteService.addFavorite("user123", "recipe123");

        verify(favoriteRepository, times(1)).save(any(FavoriteEntity.class));
        verify(recipeRepository, times(1)).save(recipe);
        assertEquals(1, recipe.getFavoriteCount());
    }

    // user-ul nu exista
    @Test
    void test_addFavorite_failure_userNotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.addFavorite("user123", "recipe123"));

        assertEquals("User not found", ex.getMessage());
    }

    // reteta nu exista
    @Test
    void test_addFavorite_failure_recipeNotFound() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.addFavorite("user123", "recipe123"));

        assertEquals("Recipe not found", ex.getMessage());
    }

    // un chef nu poate adauga la favorite
    @Test
    void test_addFavorite_failure_chefNotAllowed() {
        UserEntity user = new UserEntity();
        user.setId("chef123");
        user.setRole(UserRole.CHEF);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setFavoriteCount(0);

        when(userRepository.findById("chef123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.addFavorite("chef123", "recipe123"));

        assertEquals("Chefs cannot add recipes to favorites", ex.getMessage());
    }

    // reteta e deja in favorite
    @Test
    void test_addFavorite_failure_alreadyFavorited() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));
        when(favoriteRepository.existsByUserIdAndRecipeId("user123", "recipe123")).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.addFavorite("user123", "recipe123"));

        assertEquals("Recipe is already in favorites", ex.getMessage());
    }



    @Test
    void test_removeFavorite_success() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setFavoriteCount(1);

        FavoriteEntity favorite = new FavoriteEntity("user123", "recipe123");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));
        when(favoriteRepository.findByUserIdAndRecipeId("user123", "recipe123")).thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite("user123", "recipe123");

        verify(favoriteRepository, times(1)).delete(favorite);
        verify(recipeRepository, times(1)).save(recipe);
        assertEquals(0, recipe.getFavoriteCount());
    }

    // user-ul nu exista
    @Test
    void test_removeFavorite_failure_userNotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.removeFavorite("user123", "recipe123"));

        assertEquals("User not found", ex.getMessage());
    }

    // reteta nu exista
    @Test
    void test_removeFavorite_failure_recipeNotFound() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.removeFavorite("user123", "recipe123"));

        assertEquals("Recipe not found", ex.getMessage());
    }

    // un chef nu poate scoate o reteta de la favorite (cum nu poate nici adauga)
    @Test
    void test_removeFavorite_failure_chefNotAllowed() {
        UserEntity user = new UserEntity();
        user.setId("chef123");
        user.setRole(UserRole.CHEF);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setFavoriteCount(0);

        when(userRepository.findById("chef123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.removeFavorite("chef123", "recipe123"));

        assertEquals("Chefs cannot remove recipes from favorites", ex.getMessage());
    }

    // reteta nu e in favorite ca sa poata fi scoasa
    @Test
    void test_removeFavorite_failure_notInFavorites() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));
        when(favoriteRepository.findByUserIdAndRecipeId("user123", "recipe123")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                favoriteService.removeFavorite("user123", "recipe123"));

        assertEquals("This recipe is not in favorites", ex.getMessage());
    }

}
