package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.data.CommentRepository;
import ro.unibuc.hello.data.RecipeRepository;
import ro.unibuc.hello.data.RecipeEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final Set<String> likedComments = new HashSet<>();
    private final Set<String> reportedComments = new HashSet<>();

    public CommentService(CommentRepository commentRepository, RecipeRepository recipeRepository) {
        this.commentRepository = commentRepository;
        this.recipeRepository = recipeRepository;
    }

    public CommentEntity addComment(String userId, String recipeId, String content) {
        Optional<RecipeEntity> recipe = recipeRepository.findById(recipeId);
        if (recipe.isEmpty() || Boolean.TRUE.equals(recipe.get().getFrozen())) {
            throw new IllegalArgumentException("Cannot comment on a frozen or non-existing recipe.");
        }
        CommentEntity comment = new CommentEntity(userId, recipeId, content, 0, 0, false);
        return commentRepository.save(comment);
    }

    public List<CommentEntity> getCommentsByRecipe(String recipeId) {
        return commentRepository.findByRecipeIdOrderByLikeCountDesc(recipeId);
    }

    public CommentEntity updateComment(String commentId, String userId, String content) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own comments.");
        }
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId, String userId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own comments.");
        }
        commentRepository.delete(comment);
    }

    public CommentEntity likeComment(String commentId, String userId) {
        String likeKey = userId + "-" + commentId;
        if (likedComments.contains(likeKey)) {
            throw new IllegalArgumentException("You have already liked this comment.");
        }
        likedComments.add(likeKey);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.setLikeCount(comment.getLikeCount() + 1);
        return commentRepository.save(comment);
    }

    public CommentEntity unlikeComment(String commentId, String userId) {
        String likeKey = userId + "-" + commentId;
        if (!likedComments.contains(likeKey)) {
            throw new IllegalArgumentException("You have not liked this comment before.");
        }
        likedComments.remove(likeKey);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
        }
        return commentRepository.save(comment);
    }

    public CommentEntity reportComment(String commentId, String userId) {
        String reportKey = userId + "-" + commentId;
        if (reportedComments.contains(reportKey)) {
            throw new IllegalArgumentException("You have already reported this comment.");
        }
        reportedComments.add(reportKey);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        comment.setReportCount(comment.getReportCount() + 1);
        if (comment.getReportCount() >= 5) {
            comment.setToBeReviewed(true);
        }
        return commentRepository.save(comment);
    }
}
