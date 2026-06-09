package com.plantcare.controller;

import com.plantcare.dto.CommentDTO;
import com.plantcare.entity.Comment;
import com.plantcare.service.CommentLikeService;
import com.plantcare.service.CommentService;
import com.plantcare.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/comments", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final CommentLikeService commentLikeService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getPostComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {
        if (userId != null) {
            List<CommentDTO> comments = commentService.getPostCommentsWithLikeStatus(postId, userId, sortBy);
            return ResponseEntity.ok(comments);
        }
        return ResponseEntity.ok(commentService.getPostComments(postId, sortBy));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comment>> getUserComments(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getUserComments(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        Comment savedComment = commentService.createComment(comment);
        postService.incrementCommentCount(comment.getPostId());
        return ResponseEntity.ok(savedComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.updateComment(id, comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/best-answer")
    public ResponseEntity<Comment> setBestAnswer(@PathVariable Long id, @RequestParam boolean isBest) {
        Comment comment = commentService.setBestAnswer(id, isBest);
        return comment != null ? ResponseEntity.ok(comment) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        Comment comment = commentLikeService.likeComment(userId, id);
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }
        boolean liked = commentLikeService.isLiked(userId, id);
        return ResponseEntity.ok(Map.of(
                "comment", CommentDTO.fromEntity(comment, liked),
                "liked", liked
        ));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeComment(@PathVariable Long id, @RequestParam Long userId) {
        Comment comment = commentLikeService.unlikeComment(userId, id);
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }
        boolean liked = commentLikeService.isLiked(userId, id);
        return ResponseEntity.ok(Map.of(
                "comment", CommentDTO.fromEntity(comment, liked),
                "liked", liked
        ));
    }

    @PostMapping("/{id}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        Comment comment = commentLikeService.toggleLike(userId, id);
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }
        boolean liked = commentLikeService.isLiked(userId, id);
        return ResponseEntity.ok(Map.of(
                "comment", CommentDTO.fromEntity(comment, liked),
                "liked", liked
        ));
    }

    @GetMapping("/{id}/like-status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long id, @RequestParam Long userId) {
        boolean liked = commentLikeService.isLiked(userId, id);
        long likeCount = commentLikeService.getLikeCount(id);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }
}
