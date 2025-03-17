package ro.unibuc.hello.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestParam String userId, @RequestParam String recipeId, @RequestBody String content) {
        try {
            CommentEntity newComment = commentService.addComment(userId, recipeId, content);
            return ResponseEntity.ok(newComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get/{recipeId}")
    public ResponseEntity<List<CommentEntity>> getComments(@PathVariable String recipeId) {
        List<CommentEntity> comments = commentService.getCommentsByRecipe(recipeId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/update/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable String commentId, @RequestParam String userId, @RequestBody String content) {
        try {
            CommentEntity updatedComment = commentService.updateComment(commentId, userId, content);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId, @RequestParam String userId) {
        try {
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.ok("Comment deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/like/{commentId}")
    public ResponseEntity<?> likeComment(@PathVariable String commentId, @RequestParam String userId) {
        try {
            CommentEntity updatedComment = commentService.likeComment(commentId, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unlike/{commentId}")
    public ResponseEntity<?> unlikeComment(@PathVariable String commentId, @RequestParam String userId) {
        try {
            CommentEntity updatedComment = commentService.unlikeComment(commentId, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/report/{commentId}")
    public ResponseEntity<?> reportComment(@PathVariable String commentId, @RequestParam String userId) {
        try {
            CommentEntity updatedComment = commentService.reportComment(commentId, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
