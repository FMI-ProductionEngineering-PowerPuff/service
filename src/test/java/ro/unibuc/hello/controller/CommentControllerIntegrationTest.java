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
import ro.unibuc.hello.data.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class CommentControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20");

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
        registry.add("mongodb.connection.url", () ->
                "mongodb://localhost:" + mongoDBContainer.getMappedPort(27017));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userId;
    private String recipeId;
    private String commentId;

    @BeforeEach
    public void setUp() {
        commentRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity user = new UserEntity("testuser", "test@example.com", "Test1234", UserRole.USER, "Tester", "Bio", 25, true, false);
        userId = userRepository.save(user).getId();

        RecipeEntity recipe = new RecipeEntity(userId, "Test Recipe", "Yummy", "", "Dinner", "Main", true, false, 0);
        recipeId = recipeRepository.save(recipe).getId();

        CommentEntity comment = new CommentEntity(userId, recipeId, "First comment", 0, 0, false);
        commentId = commentRepository.save(comment).getId();
    }

    @Test
    public void testAddComment_Success() throws Exception {
        mockMvc.perform(post("/api/comments/add")
                        .param("userId", userId)
                        .param("recipeId", recipeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("New integration test comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("New integration test comment"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.reportCount").value(0));
    }

    @Test
    public void testGetComments_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/comments/get/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("First comment"));
    }

    @Test
    public void testUpdateComment_Success() throws Exception {
        mockMvc.perform(put("/api/comments/update/" + commentId)
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Updated comment content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment content"));
    }

    @Test
    public void testUpdateComment_Fails_WrongUser() throws Exception {
        UserEntity otherUser = new UserEntity("other", "other@example.com", "Pass1234", UserRole.USER, "Other", "", 22, true, false);
        String otherUserId = userRepository.save(otherUser).getId();

        mockMvc.perform(put("/api/comments/update/" + commentId)
                        .param("userId", otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Unauthorized update"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You can only edit your own comments."));
    }

    @Test
    public void testDeleteComment_Success() throws Exception {
        mockMvc.perform(delete("/api/comments/delete/" + commentId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted successfully."));
    }

    @Test
    public void testLikeComment_Success() throws Exception {
        mockMvc.perform(post("/api/comments/like/" + commentId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    public void testUnlikeComment_Success() throws Exception {
        mockMvc.perform(post("/api/comments/like/" + commentId)
                        .param("userId", userId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/comments/unlike/" + commentId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    public void testReportComment_Success() throws Exception {
        mockMvc.perform(post("/api/comments/report/" + commentId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportCount").value(1))
                .andExpect(jsonPath("$.toBeReviewed").value(false));
    }

    @Test
    public void testAddCommentFailsOnFrozenRecipe() throws Exception {
        RecipeEntity frozenRecipe = new RecipeEntity(userId, "Frozen", "Frozen test", "", "Lunch", "Side", false, true, 0);
        String frozenId = recipeRepository.save(frozenRecipe).getId();

        mockMvc.perform(post("/api/comments/add")
                        .param("userId", userId)
                        .param("recipeId", frozenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Trying to comment on frozen recipe"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot comment on a frozen or non-existing recipe."));
    }

    @Test
    public void testLikeCommentFailsOnDuplicate() throws Exception {
        mockMvc.perform(post("/api/comments/like/" + commentId)
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1));

        mockMvc.perform(post("/api/comments/like/" + commentId)
                .param("userId", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You have already liked this comment."));
    }

    @Test
    public void testReportCommentFailsOnDuplicate() throws Exception {
        mockMvc.perform(post("/api/comments/report/" + commentId)
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportCount").value(1));
                
        mockMvc.perform(post("/api/comments/report/" + commentId)
                .param("userId", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You have already reported this comment."));
    }
}
