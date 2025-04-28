package ro.unibuc.hello.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.hello.controller.CommentController;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.service.CommentService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();

        when(meterRegistry.counter(anyString())).thenReturn(mock(io.micrometer.core.instrument.Counter.class));
    }

    @Test
    void test_addComment_success() throws Exception {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Nice recipe!", 0, 0, false);
        comment.setId("1");

        when(commentService.addComment("user1", "recipe1", "Nice recipe!")).thenReturn(comment);

        mockMvc.perform(post("/api/comments/add")
                        .param("userId", "user1")
                        .param("recipeId", "recipe1")
                        .content("Nice recipe!")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.content").value("Nice recipe!"));
    }

    @Test
    void test_addComment_badRequest() throws Exception {
        mockMvc.perform(post("/api/comments/add")
                        .param("userId", "user1")
                        .param("recipeId", "recipe1")
                        .content("") // Invalid input
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest());
    }    

    @Test
    void test_getComments_success() throws Exception {
        CommentEntity c1 = new CommentEntity("user1", "recipe1", "Great!", 0, 0, false);
        c1.setId("1");
        CommentEntity c2 = new CommentEntity("user2", "recipe1", "Looks good", 0, 0, false);
        c2.setId("2");

        List<CommentEntity> comments = Arrays.asList(c1, c2);

        when(commentService.getCommentsByRecipe("recipe1")).thenReturn(comments);

        mockMvc.perform(get("/api/comments/get/recipe1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].content").value("Looks good"));
    }

    @Test
    void test_updateComment_success() throws Exception {
        CommentEntity updated = new CommentEntity("user1", "recipe1", "Updated comment", 0, 0, false);
        updated.setId("1");

        when(commentService.updateComment("1", "user1", "Updated comment")).thenReturn(updated);

        mockMvc.perform(put("/api/comments/update/1")
                        .param("userId", "user1")
                        .content("Updated comment")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));
    }

    @Test
    void test_deleteComment_success() throws Exception {
        doNothing().when(commentService).deleteComment("1", "user1");

        mockMvc.perform(delete("/api/comments/delete/1")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted successfully."));
    }

    @Test
    void test_likeComment_success() throws Exception {
        CommentEntity liked = new CommentEntity("user1", "recipe1", "Nice!", 1, 0, false);
        liked.setId("1");

        when(commentService.likeComment("1", "user2")).thenReturn(liked);

        mockMvc.perform(post("/api/comments/like/1")
                        .param("userId", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void test_unlikeComment_success() throws Exception {
        CommentEntity unliked = new CommentEntity("user1", "recipe1", "Thanks!", 0, 0, false);
        unliked.setId("1");

        when(commentService.unlikeComment("1", "user2")).thenReturn(unliked);

        mockMvc.perform(post("/api/comments/unlike/1")
                        .param("userId", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void test_reportComment_success() throws Exception {
        CommentEntity reported = new CommentEntity("user1", "recipe1", "Spam comment", 0, 1, true);
        reported.setId("1");

        when(commentService.reportComment("1", "user2")).thenReturn(reported);

        mockMvc.perform(post("/api/comments/report/1")
                        .param("userId", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }
}
