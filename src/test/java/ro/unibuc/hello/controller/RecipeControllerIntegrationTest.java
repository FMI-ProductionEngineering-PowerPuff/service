package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.data.RecipeRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.data.UserRole;
import ro.unibuc.hello.service.RecipeService;
import ro.unibuc.hello.data.FollowEntity;
import ro.unibuc.hello.data.FollowRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class RecipeControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @BeforeAll
    public static void startContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeService recipeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String TEST_USER_ID_CHEF;
    public String TEST_USER_ID_USER;
    public String TEST_USER_ID_USER_VU;
    public String TEST_USER_ID_USER_NVU;
    public String TEST_USER_ID_USER_NVA;
    public String TEST_USER_ID_CHEF_NEW1;
    public String TEST_USER_ID_CHEF_NEW2;
    public String TEST_USER_ID_CHEF_NEW3;

    private RecipeEntity createTestRecipe(String id, String userId, String name, boolean vegetarian, String type) {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(id);
        recipe.setUserId(userId);
        recipe.setName(name);
        recipe.setDescription("Delicious " + name);
        recipe.setPhoto("http://img.com/" + name + ".jpg");
        recipe.setCategory("Main");
        recipe.setType(type);
        recipe.setVegetarian(vegetarian);
        recipe.setFrozen(false);
        recipe.setFavoriteCount(0);
        return recipe;
    }

    @BeforeEach
    public void setupData() {
        objectMapper.registerModule(new JavaTimeModule()); // pt variabilele instant
        recipeRepository.deleteAll();
        userRepository.deleteAll();
        followRepository.deleteAll();

        UserEntity chef = new UserEntity("chef", "chef@example.com", "pass", UserRole.CHEF, "Chef", "Bio", 40, true, false);
        String savedChefId = userRepository.save(chef).getId(); // real ID
        this.TEST_USER_ID_CHEF = savedChefId;

        // neautorizat, fata retete
        UserEntity chefNew1 = new UserEntity("chef", "chef@example.com", "pass", UserRole.CHEF, "Chef", "Bio", 40, false, false);
        String savedChefIdNew1 = userRepository.save(chefNew1).getId(); // real ID
        this.TEST_USER_ID_CHEF_NEW1 = savedChefIdNew1;

        // fara retete
        UserEntity chefNew2 = new UserEntity("chef", "chef@example.com", "pass", UserRole.CHEF, "Chef", "Bio", 40, true, false);
        String savedChefIdNew2 = userRepository.save(chefNew2).getId(); // real ID
        this.TEST_USER_ID_CHEF_NEW2 = savedChefIdNew2;

        // 3 retete dar nu destule like uri
        UserEntity chefNew3 = new UserEntity("chef", "chef@example.com", "pass", UserRole.CHEF, "Chef", "Bio", 40, true, false);
        String savedChefIdNew3 = userRepository.save(chefNew3).getId(); // real ID
        this.TEST_USER_ID_CHEF_NEW3 = savedChefIdNew3;

        // vegetarian, adult
        UserEntity user = new UserEntity("user", "user@example.com", "pass", UserRole.USER, "User", "Bio", 25, true, true);
        String savedUserId = userRepository.save(user).getId(); // real ID
        this.TEST_USER_ID_USER = savedUserId;

        // vegetarian, underage
        UserEntity userVU = new UserEntity("user", "user@example.com", "pass", UserRole.USER, "User", "Bio", 15, true, true);
        String savedUserIdVU = userRepository.save(userVU).getId(); // real ID
        this.TEST_USER_ID_USER_VU = savedUserIdVU;

        // Non-vegetarian, underage
        UserEntity userNVU = new UserEntity("user", "user@example.com", "pass", UserRole.USER, "User", "Bio", 15, true, false);
        String savedUserIdNVU = userRepository.save(userNVU).getId(); // real ID
        this.TEST_USER_ID_USER_NVU = savedUserIdNVU;

        // Non-vegetarian, adult
        UserEntity userNVA = new UserEntity("user", "user@example.com", "pass", UserRole.USER, "User", "Bio", 25, true, false);
        String savedUserIdNVA = userRepository.save(userNVA).getId(); // real ID
        this.TEST_USER_ID_USER_NVA = savedUserIdNVA;

        RecipeEntity r1 = createTestRecipe("1", TEST_USER_ID_CHEF, "Pizza", true, "Food");
        r1.setFavoriteCount(2);
        RecipeEntity r2 = createTestRecipe("2", TEST_USER_ID_CHEF, "Soup", true, "Food");
        r2.setFavoriteCount(2);
        RecipeEntity r3 = createTestRecipe("3", TEST_USER_ID_CHEF, "Salad", true, "Food");
        r3.setFavoriteCount(1);
        RecipeEntity r4 = createTestRecipe("4", TEST_USER_ID_CHEF, "Martini", true, "Alcoholic-Drink");
        r4.setFavoriteCount(1);
        RecipeEntity r5 = createTestRecipe("5", TEST_USER_ID_CHEF_NEW3, "Steak with potatoes", false, "Food");
        RecipeEntity r6 = createTestRecipe("6", TEST_USER_ID_CHEF_NEW3, "Potatoes", true, "Food");
        RecipeEntity r7 = createTestRecipe("7", TEST_USER_ID_CHEF_NEW3, "Rice", true, "Food");

        recipeRepository.save(r1);
        recipeRepository.save(r2);
        recipeRepository.save(r3);
        recipeRepository.save(r4);
        recipeRepository.save(r5);
        recipeRepository.save(r6);
        recipeRepository.save(r7);
    }



    // get all recipes
    @Test
    void testGetAllRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_CHEF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void testGetRecipesForVegetarianAdult() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Pizza", "Soup", "Salad", "Martini", "Potatoes", "Rice")));
    }

    @Test
    void testGetRecipesForVegetarianUnderage() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_USER_VU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Pizza", "Soup", "Salad", "Potatoes", "Rice")));
    }

    @Test
    void testGetRecipesForNonVegetarianUnderage() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_USER_NVU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Pizza", "Soup", "Salad", "Steak with potatoes", "Potatoes", "Rice")));
    }

    @Test
    void testGetRecipesForNonVegetarianAdult() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_USER_NVA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Pizza", "Soup", "Salad", "Martini", "Steak with potatoes", "Potatoes", "Rice")));
    }



    // add recipe
    @Test
    void testAddRecipe() throws Exception {
        RecipeEntity newRecipe = createTestRecipe("8", TEST_USER_ID_CHEF, "Pasta", true, "Food");

        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta"));
    }

    @Test
    void testAddRecipeByNonexistantUser_Fails() throws Exception {
        RecipeEntity newRecipe = new RecipeEntity();
        newRecipe.setUserId("nonexistent-user-id"); // user ul nu exista
        newRecipe.setName("Smoothie");
        newRecipe.setDescription("Healthy smoothie");
        newRecipe.setPhoto("http://img.com/smoothie.jpg");
        newRecipe.setCategory("Drink");
        newRecipe.setType("Non-Alcoholic-Drink");
        newRecipe.setVegetarian(true);

        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found."));
    }

    @Test
    void testAddRecipeByNonChefUser_Fails() throws Exception {
        RecipeEntity newRecipe = new RecipeEntity();
        newRecipe.setUserId(TEST_USER_ID_USER); // user ul nu e chef
        newRecipe.setName("Smoothie");
        newRecipe.setDescription("Healthy smoothie");
        newRecipe.setPhoto("http://img.com/smoothie.jpg");
        newRecipe.setCategory("Drink");
        newRecipe.setType("Non-Alcoholic-Drink");
        newRecipe.setVegetarian(true);

        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only chefs can add recipes."));
    }

    @Test
    void testAddRecipeWithInvalidType_Fails() throws Exception {
        RecipeEntity newRecipe = new RecipeEntity();
        newRecipe.setUserId(TEST_USER_ID_CHEF);
        newRecipe.setName("Mystery Dish");
        newRecipe.setDescription("Strange dish");
        newRecipe.setPhoto("http://img.com/mystery.jpg");
        newRecipe.setCategory("Main");
        newRecipe.setType("INVALID-TYPE"); // type urile valide sunt Food, Alcoholic-Drink, Non-Alcoholic-Drink
        newRecipe.setVegetarian(false);

        mockMvc.perform(post("/api/recipes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid type. Type must be 'Food', 'Alcoholic-Drink' or Non-Alcoholic-Drink'."));
    }



    // update recipe
    @Test
    void testUpdateRecipe() throws Exception {
        RecipeEntity updated = createTestRecipe("1", TEST_USER_ID_CHEF, "Updated Pizza", false, "Food");

        mockMvc.perform(put("/api/recipes/update/1")
                        .param("userId", TEST_USER_ID_CHEF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pizza"));
    }



    // delete recipe
    @Test
    void testDeleteRecipe() throws Exception {
        mockMvc.perform(delete("/api/recipes/delete/1")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/recipes/get-recipes/" + TEST_USER_ID_CHEF))
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void testDeleteRecipe_NotAuthor_Fails() throws Exception {
        mockMvc.perform(delete("/api/recipes/delete/1")
                        .param("userId", TEST_USER_ID_USER)) // USER incearca sa stearga reteta
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only the author can delete this recipe."));
    }

    @Test
    void testDeleteRecipe_InvalidRecipeId_Fails() throws Exception {
        mockMvc.perform(delete("/api/recipes/delete/invalid-id")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe not found."));
    }



    // get recipe by id
    @Test
    void testGetRecipeById() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipe/1")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    void testGetAlcoholicRecipe_UnderageUser_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipe/4") // Martini
                        .param("userId", TEST_USER_ID_USER_VU)) // Underage vegetarian user
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You must be 18+ to view this alcoholic drink recipe."));
    }

    @Test
    void testGetNonVegetarianRecipe_VegetarianUser_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipe/5") // Steak with potatoes
                        .param("userId", TEST_USER_ID_USER)) // Adult vegetarian user
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This recipe is not vegetarian."));
    }

    @Test
    void testGetRecipe_InvalidUser_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipe/1")
                        .param("userId", "nonexistent-user-id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found."));
    }

    @Test
    void testGetRecipe_InvalidRecipe_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/get-recipe/invalid-recipe-id")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe not found."));
    }



    // change frozen status
    @Test
    void testChangeFrozenStatus() throws Exception {
        mockMvc.perform(post("/api/recipes/change-status/1")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frozen").value(true));
    }

    @Test
    void testChangeFrozenStatusByNonAuthor_Fails() throws Exception {
        mockMvc.perform(post("/api/recipes/change-status/1")
                        .param("userId", TEST_USER_ID_USER)) // nu are cum sa fie autor un user deci un user nu poate schimba frozen statusul unei retete pt ca nu e autor
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only the author can change the frozen status."));
    }



    // get featured recipe
    @Test
    void testGetFeaturedRecipe() throws Exception {
        mockMvc.perform(get("/api/recipes/featured/" + TEST_USER_ID_CHEF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID_CHEF));
    }

    @Test
    void testGetFeaturedRecipe_UnauthorizedChef_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/featured/" + TEST_USER_ID_CHEF_NEW1))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User is not an authorized chef"));
    }

    @Test
    void testGetFeaturedRecipe_NoRecipesChef_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/featured/" + TEST_USER_ID_CHEF_NEW2))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chef must have at least 3 recipes to have a featured recipe"));
    }

    @Test
    void testGetFeaturedRecipe_NotEnoughLikesChef_Fails() throws Exception {
        mockMvc.perform(get("/api/recipes/featured/" + TEST_USER_ID_CHEF_NEW3))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chef's recipes must have at least 4 likes in total"));
    }



    // FAVORITES
    @Test
    void testAddFavorite_Success() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe added to favorites successfully!"));
    }    

    @Test
    void testAddFavorite_UserNotFound() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", "invalid-user"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }    

    @Test
    void testAddFavorite_RecipeNotFound() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/invalid-recipe-id")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe not found"));
    }    

    @Test
    void testAddFavorite_ChefCannotFavorite() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chefs cannot add recipes to favorites"));
    }    

    @Test
    void testAddFavorite_AlreadyFavorited() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isOk());
    
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe is already in favorites"));
    }    

    @Test
    void testRemoveFavorite_Success() throws Exception {
        mockMvc.perform(post("/api/recipes/add-to-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isOk());
    
        mockMvc.perform(delete("/api/recipes/remove-from-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isOk())
                .andExpect(content().string("Recipe removed from favorites successfully!"));
    }    

    @Test
    void testRemoveFavorite_NotInFavorites() throws Exception {
        mockMvc.perform(delete("/api/recipes/remove-from-favorites/1")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This recipe is not in favorites"));
    }

    @Test
    void testRemoveFavorite_ChefCannotUnfavorite() throws Exception {
        mockMvc.perform(delete("/api/recipes/remove-from-favorites/1")
                        .param("userId", TEST_USER_ID_CHEF))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chefs cannot remove recipes from favorites"));
    }

    @Test
    void testRemoveFavorite_InvalidUser() throws Exception {
        mockMvc.perform(delete("/api/recipes/remove-from-favorites/1")
                        .param("userId", "invalid-user"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    void testRemoveFavorite_InvalidRecipe() throws Exception {
        mockMvc.perform(delete("/api/recipes/remove-from-favorites/invalid-recipe")
                        .param("userId", TEST_USER_ID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Recipe not found"));
    }

    @Test
    void testChefAddsValidContributorChef() throws Exception {
        // contributor chef
        UserEntity chefContributor = new UserEntity("chefContributor", "contrib@example.com", "pass", UserRole.CHEF, "ContribChef", "Bio", 35, true, true);
        String chefContributorId = userRepository.save(chefContributor).getId();

        // add 2 followers for contributor
        UserEntity follower1 = new UserEntity("f1", "f1@example.com", "pass", UserRole.USER, "F1", "Bio", 20, true, true);
        UserEntity follower2 = new UserEntity("f2", "f2@example.com", "pass", UserRole.USER, "F2", "Bio", 22, true, true);
        String f1Id = userRepository.save(follower1).getId();
        String f2Id = userRepository.save(follower2).getId();

        followRepository.save(new FollowEntity(f1Id, chefContributorId));
        followRepository.save(new FollowEntity(f2Id, chefContributorId));

        // add 3 recipes for the contributor
        for (int i = 1; i <= 3; i++) {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setUserId(chefContributorId);
            recipe.setName("Contributor Recipe " + i);
            recipe.setDescription("Yummy");
            recipe.setPhoto("photo" + i + ".jpg");
            recipe.setCategory("Main");
            recipe.setType("Food");
            recipe.setVegetarian(true);
            recipe.setFrozen(false);
            recipe.setFavoriteCount(1);
            recipeRepository.save(recipe);
        }

        // create a recipe owned by the chefOwner
        RecipeEntity ownerRecipe = new RecipeEntity();
        ownerRecipe.setUserId(TEST_USER_ID_CHEF);
        ownerRecipe.setName("Owner's Recipe");
        ownerRecipe.setDescription("Great");
        ownerRecipe.setPhoto("owner.jpg");
        ownerRecipe.setCategory("Main");
        ownerRecipe.setType("Food");
        ownerRecipe.setVegetarian(false);
        ownerRecipe.setFrozen(false);
        ownerRecipe.setFavoriteCount(0);
        String recipeId = recipeRepository.save(ownerRecipe).getId();

        mockMvc.perform(post("/api/recipes/add-contributor")
                .param("loggedInUserId", TEST_USER_ID_CHEF)
                .param("chefId", chefContributorId)
                .param("recipeId", recipeId))
                .andExpect(status().isOk())
                .andExpect(content().string("Contributor added successfully."));
    }

    @Test
    void testChefRemovesContributorChef() throws Exception {
        // contributor chef
        UserEntity chefContributor = new UserEntity("chefContributor2", "contrib2@example.com", "pass", UserRole.CHEF, "ContribChef2", "Bio", 36, true, true);
        String chefContributorId = userRepository.save(chefContributor).getId();

        // add 2 followers
        UserEntity f1 = new UserEntity("f3", "f3@example.com", "pass", UserRole.USER, "F3", "Bio", 21, true, true);
        UserEntity f2 = new UserEntity("f4", "f4@example.com", "pass", UserRole.USER, "F4", "Bio", 22, true, true);
        String f1Id = userRepository.save(f1).getId();
        String f2Id = userRepository.save(f2).getId();

        followRepository.save(new FollowEntity(f1Id, chefContributorId));
        followRepository.save(new FollowEntity(f2Id, chefContributorId));

        // add 3 recipes to qualify as a contributor
        for (int i = 1; i <= 3; i++) {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setUserId(chefContributorId);
            recipe.setName("Cont Recipe " + i);
            recipe.setDescription("Recipe " + i);
            recipe.setPhoto("img" + i + ".jpg");
            recipe.setCategory("Main");
            recipe.setType("Food");
            recipe.setVegetarian(false);
            recipe.setFrozen(false);
            recipe.setFavoriteCount(i);
            recipeRepository.save(recipe);
        }

        // recipe owned by TEST_USER_ID_CHEF
        RecipeEntity ownerRecipe = new RecipeEntity();
        ownerRecipe.setUserId(TEST_USER_ID_CHEF);
        ownerRecipe.setName("Owned Recipe");
        ownerRecipe.setDescription("Something");
        ownerRecipe.setPhoto("photo.jpg");
        ownerRecipe.setCategory("Main");
        ownerRecipe.setType("Food");
        ownerRecipe.setVegetarian(false);
        ownerRecipe.setFrozen(false);
        ownerRecipe.setFavoriteCount(0);
        String recipeId = recipeRepository.save(ownerRecipe).getId();

        // add contributor
        mockMvc.perform(post("/api/recipes/add-contributor")
                .param("loggedInUserId", TEST_USER_ID_CHEF)
                .param("chefId", chefContributorId)
                .param("recipeId", recipeId))
                .andExpect(status().isOk())
                .andExpect(content().string("Contributor added successfully."));

        // remove contributor
        mockMvc.perform(delete("/api/recipes/remove-contributor")
                .param("loggedInUserId", TEST_USER_ID_CHEF)
                .param("chefId", chefContributorId)
                .param("recipeId", recipeId))
                .andExpect(status().isOk())
                .andExpect(content().string("Contributor removed successfully."));
    }
}
