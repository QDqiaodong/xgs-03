package com.plantcare.service;

import com.plantcare.entity.Comment;
import com.plantcare.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getPostComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public List<Comment> getUserComments(Long userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment comment) {
        comment.setId(id);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public Comment setBestAnswer(Long commentId, boolean isBest) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setIsBestAnswer(isBest);
            return commentRepository.save(comment);
        }
        return null;
    }
}
