package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.service.FavoriteService;
import ro.unibuc.hello.service.RecipeService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



class RecipeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecipeService recipeService;

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private RecipeController recipeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();

        objectMapper.registerModule(new JavaTimeModule()); // pt variabila de tipul instant, createdAt
    }

    @Test
    void test_addRecipe_success() throws Exception {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Cake");
        recipe.setUserId("user123");
        recipe.setType("Food");
        recipe.setCategory("Dessert");
        recipe.setVegetarian(false);
        recipe.setFrozen(false);
        recipe.setFavoriteCount(0);
        recipe.setDescription("Yummy!");
    
        when(recipeService.addRecipe(any(RecipeEntity.class)))
                .thenReturn(Optional.of(recipe));
    
        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cake"));
    }

    @Test
    void test_addRecipe_failure() throws Exception {
        RecipeEntity invalidRecipe = new RecipeEntity(); // lipsesc campuri obligatorii cum sunt Type, userId
    
        when(recipeService.addRecipe(any(RecipeEntity.class)))
                .thenThrow(new IllegalArgumentException("User not found."));
    
        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found."));
    }
    


    @Test
    void test_changeFrozenStatus_success() throws Exception {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Cake");
        recipe.setFrozen(true);

        when(recipeService.changeFrozenStatus("recipe123", "user123"))
                .thenReturn(Optional.of(recipe));

        mockMvc.perform(post("/api/recipes/change-status/recipe123")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cake"))
                .andExpect(jsonPath("$.frozen").value(true));
    }

    @Test
    void test_changeFrozenStatus_failure() throws Exception {
        when(recipeService.changeFrozenStatus("recipe123", "user123"))
                .thenThrow(new IllegalArgumentException("Only the author can change the frozen status."));

        mockMvc.perform(post("/api/recipes/change-status/recipe123")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only the author can change the frozen status."));
    }



    @Test
    void test_getRecipes_success() throws Exception {
        RecipeEntity recipe1 = new RecipeEntity();
        recipe1.setName("Recipe 1");
        RecipeEntity recipe2 = new RecipeEntity();
        recipe2.setName("Recipe 2");

        when(recipeService.getFilteredRecipes("user123")).thenReturn(List.of(recipe1, recipe2));

        mockMvc.perform(get("/api/recipes/get-recipes/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }



    @Test
    void test_getRecipeById_success() throws Exception {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Cake");

        when(recipeService.getRecipeById("recipe123", "user123")).thenReturn(Optional.of(recipe));

        mockMvc.perform(get("/api/recipes/get-recipe/recipe123")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cake"));
    }



    @Test
    void test_updateRecipe_success() throws Exception {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName("Cake");

        when(recipeService.updateRecipe(eq("recipe123"), eq("user123"), any(RecipeEntity.class)))
                .thenReturn(Optional.of(recipe));

        mockMvc.perform(put("/api/recipes/update/recipe123")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cake"));
    }

    @Test
    void test_updateRecipe_failure() throws Exception {
        RecipeEntity updatedRecipe = new RecipeEntity();
        updatedRecipe.setName("Cake");

        when(recipeService.updateRecipe(eq("recipe123"), eq("user123"), any(RecipeEntity.class)))
                .thenThrow(new IllegalArgumentException("Only the author and contributors can update this recipe."));

        mockMvc.perform(put("/api/recipes/update/recipe123")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only the author and contributors can update this recipe."));
    }



    @Test
    void test_deleteRecipe_success() throws Exception {
        doNothing().when(recipeService).deleteRecipe("recipe123", "user123");

        mockMvc.perform(delete("/api/recipes/delete/recipe123")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe deleted successfully."));
    }

    @Test
    void test_deleteRecipe_failure() throws Exception {
        doThrow(new IllegalArgumentException("Only the author can delete this recipe."))
                .when(recipeService).deleteRecipe("recipe123", "user123");

        mockMvc.perform(delete("/api/recipes/delete/recipe123")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only the author can delete this recipe."));
    }



    @Test
    void test_getFeaturedRecipe_success() throws Exception {
        RecipeEntity featuredRecipe = new RecipeEntity();
        featuredRecipe.setName("Cake");

        when(recipeService.getFeaturedRecipe("chef123")).thenReturn(featuredRecipe);

        mockMvc.perform(get("/api/recipes/featured/chef123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cake"));
    }

    @Test
    void test_getFeaturedRecipe_failure() throws Exception {
        when(recipeService.getFeaturedRecipe("chef123"))
                .thenThrow(new IllegalArgumentException("Chef not eligible for featured recipe"));

        mockMvc.perform(get("/api/recipes/featured/chef123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chef not eligible for featured recipe"));
    }



    // Favorite
    @Test
    void test_addFavorite_success() throws Exception {
        doNothing().when(favoriteService).addFavorite("user123", "recipe456");

        mockMvc.perform(post("/api/recipes/add-to-favorites/recipe456")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe added to favorites successfully!"));
    }

    @Test
    void test_addFavorite_failure() throws Exception {
        doThrow(new IllegalArgumentException("Recipe not found"))
                .when(favoriteService).addFavorite("user123", "recipe999");

        mockMvc.perform(post("/api/recipes/add-to-favorites/recipe999")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe not found"));
    }

    @Test
    void test_removeFavorite_success() throws Exception {
        doNothing().when(favoriteService).removeFavorite("user123", "recipe456");

        mockMvc.perform(delete("/api/recipes/remove-from-favorites/recipe456")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe removed from favorites successfully!"));
    }

    @Test
    void test_removeFavorite_failure() throws Exception {
        doThrow(new IllegalArgumentException("Favorite not found"))
                .when(favoriteService).removeFavorite("user123", "recipe999");

        mockMvc.perform(delete("/api/recipes/remove-from-favorites/recipe999")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite not found"));
    }

}
