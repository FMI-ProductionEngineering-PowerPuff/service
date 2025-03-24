package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ContributorRepository contributorRepository;

    @InjectMocks
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void test_addRecipe_success() {
        UserEntity chef = new UserEntity();
        chef.setId("user123");
        chef.setRole(UserRole.CHEF);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setUserId("user123");
        recipe.setType("Food");

        when(userRepository.findById("user123")).thenReturn(Optional.of(chef));
        when(recipeRepository.save(any(RecipeEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<RecipeEntity> result = recipeService.addRecipe(recipe);

        assertTrue(result.isPresent());
        assertFalse(result.get().getFrozen());
        assertEquals(0, result.get().getFavoriteCount());
    }

    // user-ul nu exista
    @Test
    void test_addRecipe_failure_userNotFound() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setUserId("user123");
        recipe.setType("Food");

        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> recipeService.addRecipe(recipe));
        assertEquals("User not found.", exception.getMessage());
    }

    // user-ul nu e CHEF
    @Test
    void test_addRecipe_failure_notChef() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setRole(UserRole.USER); // nu e CHEF, e USER

        RecipeEntity recipe = new RecipeEntity();
        recipe.setUserId("user123");
        recipe.setType("Food");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> recipeService.addRecipe(recipe));
        assertEquals("Only chefs can add recipes.", exception.getMessage());
    }

    // user-ul nu a pus un type valid
    @Test
    void test_addRecipe_failure_invalidType() {
        UserEntity chef = new UserEntity();
        chef.setId("user123");
        chef.setRole(UserRole.CHEF);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setUserId("user123");
        recipe.setType("InvalidType"); // type poate sa fie Food, Non-Alcoholic-Drink sau Alcoholic-Drink

        when(userRepository.findById("user123")).thenReturn(Optional.of(chef));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> recipeService.addRecipe(recipe));
        assertEquals("Invalid type. Type must be 'Food', 'Alcoholic-Drink' or Non-Alcoholic-Drink'.", exception.getMessage());
    }



    @Test
    void test_changeFrozenStatus_success() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setUserId("user123");
        recipe.setFrozen(false);

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(RecipeEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Simulare
        Optional<RecipeEntity> result = recipeService.changeFrozenStatus("recipe123", "user123");

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getFrozen()); // frozen trb sa se fi schimbat la true
        verify(recipeRepository, times(1)).save(recipe);
    }

    // reteta nu exista
    @Test
    void test_changeFrozenStatus_failure_recipeNotFound() {
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.changeFrozenStatus("recipe123", "user123"));

        assertEquals("Recipe not found.", exception.getMessage());
    }

    // user-ul nu e autorul retetei
    @Test
    void test_changeFrozenStatus_failure_notAuthor() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setUserId("author123"); // diferit de "user123" care e dat ca parametru functiei din service
        recipe.setFrozen(false);

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.changeFrozenStatus("recipe123", "user123"));

        assertEquals("Only the author can change the frozen status.", exception.getMessage());
    }



    // get pentru un adult (18+) care nu e vegetarian
    @Test
    void test_getFilteredRecipes_success_noFiltering() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setAge(25);
        user.setVegetarian(false);

        RecipeEntity r1 = new RecipeEntity();
        r1.setType("Food");
        r1.setVegetarian(true);

        RecipeEntity r2 = new RecipeEntity();
        r2.setType("Alcoholic-Drink");
        r2.setVegetarian(false);

        List<RecipeEntity> allRecipes = List.of(r1, r2);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findAllByOrderByFavoriteCountDesc()).thenReturn(allRecipes);

        List<RecipeEntity> result = recipeService.getFilteredRecipes("user123");

        assertEquals(2, result.size());
    }

    // get pentru un user care nu exista
    @Test
    void test_getFilteredRecipes_failure_userNotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getFilteredRecipes("user123"));

        assertEquals("User not found.", exception.getMessage());
    }

    // get pentru un user mai tanar de 18 ani, vegetarian
    @Test
    void test_getFilteredRecipes_filtersApplied() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setAge(17);
        user.setVegetarian(true);

        RecipeEntity r1 = new RecipeEntity();
        r1.setType("Alcoholic-Drink"); // sa nu apara pt ca e alcool
        r1.setVegetarian(true);

        RecipeEntity r2 = new RecipeEntity();
        r2.setType("Food");
        r2.setVegetarian(false); // sa nu apara pt ca nu e vegetariana

        RecipeEntity r3 = new RecipeEntity();
        r3.setType("Food");
        r3.setVegetarian(true);

        List<RecipeEntity> allRecipes = List.of(r1, r2, r3);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findAllByOrderByFavoriteCountDesc()).thenReturn(allRecipes);

        List<RecipeEntity> result = recipeService.getFilteredRecipes("user123");

        assertEquals(1, result.size());
        assertTrue(result.contains(r3));
    }



    @Test
    void test_getRecipeById_success() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setAge(25);
        user.setVegetarian(false);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setType("Food");
        recipe.setVegetarian(false);

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Optional<RecipeEntity> result = recipeService.getRecipeById("recipe123", "user123");

        assertTrue(result.isPresent());
        assertEquals("recipe123", result.get().getId());
    }

    // user-ul nu exista
    @Test
    void test_getRecipeById_failure_userNotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getRecipeById("recipe123", "user123"));

        assertEquals("User not found.", exception.getMessage());
    }

    // reteta nu exista
    @Test
    void test_getRecipeById_failure_recipeNotFound() {
        UserEntity user = new UserEntity();
        user.setId("user123");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getRecipeById("recipe123", "user123"));

        assertEquals("Recipe not found.", exception.getMessage());
    }

    // user-ul are sub 18 ani si reteta e alcoolica
    @Test
    void test_getRecipeById_failure_userUnderageForAlcohol() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setAge(17);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setType("Alcoholic-Drink");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getRecipeById("recipe123", "user123"));

        assertEquals("You must be 18+ to view this alcoholic drink recipe.", exception.getMessage());
    }

    // user-ul e vegetarian si reteta nu e
    @Test
    void test_getRecipeById_failure_vegetarianRestriction() {
        UserEntity user = new UserEntity();
        user.setId("user123");
        user.setAge(25);
        user.setVegetarian(true);

        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setType("Food");
        recipe.setVegetarian(false); // not allowed

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getRecipeById("recipe123", "user123"));

        assertEquals("This recipe is not vegetarian.", exception.getMessage());
    }



    @Test
    void test_updateRecipe_success_asAuthor() {
        RecipeEntity existing = new RecipeEntity();
        existing.setId("recipe123");
        existing.setUserId("user123");

        RecipeEntity updated = new RecipeEntity();
        updated.setName("Updated Name");
        updated.setDescription("Updated Desc");
        updated.setPhoto("new-photo.jpg");
        updated.setCategory("New Category");
        updated.setType("Food");
        updated.setVegetarian(true);
        updated.setFrozen(true);

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(existing));
        when(contributorRepository.findByUserIdAndRecipeId("user123", "recipe123")).thenReturn(Optional.empty());
        when(recipeRepository.save(any(RecipeEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<RecipeEntity> result = recipeService.updateRecipe("recipe123", "user123", updated);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("Food", result.get().getType());
    }

    // reteta nu exista
    @Test
    void test_updateRecipe_failure_recipeNotFound() {
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        RecipeEntity updated = new RecipeEntity();
        updated.setType("Food");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.updateRecipe("recipe123", "user123", updated));

        assertEquals("Recipe not found.", exception.getMessage());
    }

    // user-ul nu e autor sau contributor
    @Test
    void test_updateRecipe_failure_notAuthorOrContributor() {
        RecipeEntity existing = new RecipeEntity();
        existing.setId("recipe123");
        existing.setUserId("author123");

        RecipeEntity updated = new RecipeEntity();
        updated.setType("Food");

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(existing));
        when(contributorRepository.findByUserIdAndRecipeId("user123", "recipe123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.updateRecipe("recipe123", "user123", updated));

        assertEquals("Only the author and contributors can update this recipe.", exception.getMessage());
    }

    // type invalid pt reteta
    @Test
    void test_updateRecipe_failure_invalidType() {
        RecipeEntity existing = new RecipeEntity();
        existing.setId("recipe123");
        existing.setUserId("user123");

        RecipeEntity updated = new RecipeEntity();
        updated.setType("InvalidType");

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(existing));
        when(contributorRepository.findByUserIdAndRecipeId("user123", "recipe123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.updateRecipe("recipe123", "user123", updated));

        assertEquals("Invalid type. Type must be 'Food', 'Alcoholic-Drink' or 'Non-Alcoholic-Drink'.", exception.getMessage());
    }



    @Test
    void test_deleteRecipe_success_asAuthor() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setUserId("user123");

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe("recipe123", "user123");

        verify(recipeRepository, times(1)).delete(recipe);
    }

    // reteta nu exista
    @Test
    void test_deleteRecipe_failure_recipeNotFound() {
        when(recipeRepository.findById("recipe123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.deleteRecipe("recipe123", "user123"));

        assertEquals("Recipe not found.", exception.getMessage());
    }

    // cineva care nu e autorul incearca sa stearga reteta
    @Test
    void test_deleteRecipe_failure_notAuthor() {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId("recipe123");
        recipe.setUserId("author123"); // diferit de "user123"

        when(recipeRepository.findById("recipe123")).thenReturn(Optional.of(recipe));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.deleteRecipe("recipe123", "user123"));

        assertEquals("Only the author can delete this recipe.", exception.getMessage());
    }



    @Test
    void test_getFeaturedRecipe_success() {
        UserEntity chef = new UserEntity();
        chef.setId("chef123");

        RecipeEntity featured = new RecipeEntity();
        featured.setId("recipe123");

        when(userRepository.findByIdAndRoleAndAuthorization("chef123", UserRole.CHEF, true)).thenReturn(Optional.of(chef));
        when(recipeRepository.countByUserId("chef123")).thenReturn(3L);
        when(recipeRepository.getTotalLikesByUserId("chef123")).thenReturn(Optional.of(5L));
        when(recipeRepository.findRandomRecipeByUserId("chef123")).thenReturn(Optional.of(featured));

        RecipeEntity result = recipeService.getFeaturedRecipe("chef123");

        assertNotNull(result);
        assertEquals("recipe123", result.getId());
    }

    // nu e chef autorizat
    @Test
    void test_getFeaturedRecipe_failure_unauthorizedChef() {
        when(userRepository.findByIdAndRoleAndAuthorization("chef123", UserRole.CHEF, true))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getFeaturedRecipe("chef123"));

        assertEquals("User is not an authorized chef", exception.getMessage());
    }

    // chef-ul care mai putin de 3 retete postate
    @Test
    void test_getFeaturedRecipe_failure_notEnoughRecipes() {
        UserEntity chef = new UserEntity();
        chef.setId("chef123");

        when(userRepository.findByIdAndRoleAndAuthorization("chef123", UserRole.CHEF, true))
                .thenReturn(Optional.of(chef));
        when(recipeRepository.countByUserId("chef123")).thenReturn(2L);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getFeaturedRecipe("chef123"));

        assertEquals("Chef must have at least 3 recipes to have a featured recipe", exception.getMessage());
    }

    // chef-ul are mai putin de 4 like-uri primite
    @Test
    void test_getFeaturedRecipe_failure_notEnoughLikes() {
        UserEntity chef = new UserEntity();
        chef.setId("chef123");

        when(userRepository.findByIdAndRoleAndAuthorization("chef123", UserRole.CHEF, true))
                .thenReturn(Optional.of(chef));
        when(recipeRepository.countByUserId("chef123")).thenReturn(3L);
        when(recipeRepository.getTotalLikesByUserId("chef123")).thenReturn(Optional.of(2L));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getFeaturedRecipe("chef123"));

        assertEquals("Chef's recipes must have at least 4 likes in total", exception.getMessage());
    }

    // nu exista reteta pt acel chef (nu are cum pt ca deja s-a veficat ca el sa aiba minim 3 retete...)
    @Test
    void test_getFeaturedRecipe_failure_noRecipeReturned() {
        UserEntity chef = new UserEntity();
        chef.setId("chef123");

        when(userRepository.findByIdAndRoleAndAuthorization("chef123", UserRole.CHEF, true))
                .thenReturn(Optional.of(chef));
        when(recipeRepository.countByUserId("chef123")).thenReturn(3L);
        when(recipeRepository.getTotalLikesByUserId("chef123")).thenReturn(Optional.of(5L));
        when(recipeRepository.findRandomRecipeByUserId("chef123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                recipeService.getFeaturedRecipe("chef123"));

        assertEquals("No recipes found for this chef", exception.getMessage());
    }

}