package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.data.CommentRepository;
import ro.unibuc.hello.data.RecipeEntity;
import ro.unibuc.hello.data.RecipeRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddComment_validRecipe_shouldSave() {
        String userId = "user1";
        String recipeId = "recipe1";
        String content = "Nice recipe!";
        RecipeEntity recipe = new RecipeEntity();
        recipe.setFrozen(false);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentEntity result = commentService.addComment(userId, recipeId, content);

        assertEquals(userId, result.getUserId());
        assertEquals(content, result.getContent());
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    void testAddComment_frozenRecipe_shouldThrow() {
        String recipeId = "recipe1";
        RecipeEntity frozenRecipe = new RecipeEntity();
        frozenRecipe.setFrozen(true);
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(frozenRecipe));

        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment("user1", recipeId, "content"));
    }

    @Test
    void testAddComment_nonExistentRecipe_shouldThrow() {
        when(recipeRepository.findById("recipeX")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment("user1", "recipeX", "content"));
    }

    @Test
    void testGetCommentsByRecipe_shouldReturnSortedList() {
        String recipeId = "recipe1";

        // Correct order: B has more likes
        List<CommentEntity> mockComments = List.of(
                new CommentEntity("user2", recipeId, "B", 5, 0, false),
                new CommentEntity("user1", recipeId, "A", 2, 0, false)
        );

        when(commentRepository.findByRecipeIdOrderByLikeCountDesc(recipeId)).thenReturn(mockComments);

        List<CommentEntity> result = commentService.getCommentsByRecipe(recipeId);

        assertEquals(2, result.size());
        assertEquals("B", result.get(0).getContent()); // Most liked should be first
    }

    @Test
    void testUpdateComment_valid_shouldUpdateContent() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Old content", 0, 0, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommentEntity updated = commentService.updateComment("c1", "user1", "New content");

        assertEquals("New content", updated.getContent());
    }

    @Test
    void testUpdateComment_wrongUser_shouldThrow() {
        CommentEntity comment = new CommentEntity("userX", "recipe1", "Hello", 0, 0, false);
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () ->
                commentService.updateComment("c1", "userY", "New content"));
    }

    @Test
    void testDeleteComment_valid_shouldDelete() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Bye", 0, 0, false);
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));

        commentService.deleteComment("c1", "user1");

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_wrongUser_shouldThrow() {
        CommentEntity comment = new CommentEntity("userX", "recipe1", "Bye", 0, 0, false);
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class, () ->
                commentService.deleteComment("c1", "userY"));
    }

    @Test
    void testLikeComment_notYetLiked_shouldIncreaseCount() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Like me", 0, 0, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommentEntity result = commentService.likeComment("c1", "liker");

        assertEquals(1, result.getLikeCount());
    }

    @Test
    void testLikeComment_twice_shouldThrow() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Hello", 0, 0, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.likeComment("c1", "userA");

        assertThrows(IllegalArgumentException.class, () ->
                commentService.likeComment("c1", "userA"));
    }

    @Test
    void testUnlikeComment_shouldDecreaseCount() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Hi", 0, 0, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.likeComment("c1", "userA");

        comment.setLikeCount(1);
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));

        CommentEntity result = commentService.unlikeComment("c1", "userA");

        assertEquals(0, result.getLikeCount());
    }


    @Test
    void testUnlikeComment_notLikedBefore_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.unlikeComment("c1", "userX"));
    }

    @Test
    void testReportComment_fiveTimes_shouldSetToBeReviewed() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Report me", 0, 4, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.reportComment("c1", "user5");

        assertTrue(comment.getToBeReviewed());
        assertEquals(5, comment.getReportCount());
    }

    @Test
    void testReportComment_duplicateReport_shouldThrow() {
        CommentEntity comment = new CommentEntity("user1", "recipe1", "Spam", 0, 1, false);
        comment.setId("c1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.reportComment("c1", "userA");

        assertThrows(IllegalArgumentException.class, () ->
                commentService.reportComment("c1", "userA"));
    }
}
